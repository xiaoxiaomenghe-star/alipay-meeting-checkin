param([string]$BaseUrl = "http://localhost:8088")

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param([string]$Method,[string]$Path,[hashtable]$Headers=@{},[object]$Body=$null)
    $params = @{ Method=$Method; Uri="$BaseUrl$Path"; Headers=$Headers; ContentType="application/json" }
    if ($null -ne $Body) { $params.Body = ($Body | ConvertTo-Json -Depth 8 -Compress) }
    Invoke-RestMethod @params
}

$login = Invoke-Api -Method Post -Path "/api/v1/auth/login" -Body @{authCode="dev-alice"}
if ($login.code -ne "OK" -or [string]::IsNullOrWhiteSpace($login.data.token)) { throw "Login verification failed" }
$headers = @{Authorization="Bearer $($login.data.token)"; "X-Request-Id"="compose-e2e-001"}

$meetings = Invoke-Api -Method Get -Path "/api/v1/meetings" -Headers $headers
if ($meetings.data.Count -ne 1) { throw "Meeting list verification failed" }
$detail = Invoke-Api -Method Get -Path "/api/v1/meetings/1" -Headers $headers
$entryToken = $detail.data.points[0].entryToken
if ([string]::IsNullOrWhiteSpace($entryToken)) { throw "Entry token verification failed" }

$registration = Invoke-Api -Method Post -Path "/api/v1/meetings/1/registrations" -Headers $headers
if ($registration.data.status -ne "REGISTERED") { throw "Registration verification failed" }

$records = Invoke-Api -Method Get -Path "/api/v1/me/check-ins" -Headers $headers
$reusedExistingCheckin = $records.data.Count -gt 0
$traceId = $null
if (-not $reusedExistingCheckin) {
    $checkinBody = @{entryToken=$entryToken;entryType="QR";latitude=31.03;longitude=121.22}
    $checkin = Invoke-Api -Method Post -Path "/api/v1/check-ins" -Headers $headers -Body $checkinBody
    if ($checkin.code -ne "OK" -or $checkin.data.pointsPending -ne 20) { throw "Check-in verification failed" }
    $traceId = $checkin.traceId
}

$deadline = (Get-Date).AddSeconds(20)
do {
    $balance = Invoke-Api -Method Get -Path "/api/v1/me/points" -Headers $headers
    if ($balance.data.balance -eq 20) { break }
    Start-Sleep -Milliseconds 500
} while ((Get-Date) -lt $deadline)
if ($balance.data.balance -ne 20) { throw "Points persistence or asynchronous delivery verification failed" }

$records = Invoke-Api -Method Get -Path "/api/v1/me/check-ins" -Headers $headers
$ledger = Invoke-Api -Method Get -Path "/api/v1/me/points/ledger" -Headers $headers
if ($records.data.Count -ne 1 -or $ledger.data.Count -ne 1) { throw "Record or ledger count verification failed" }
if (-not $traceId) { $traceId = $records.data[0].traceId }

$duplicateCode = $null
$originalEntryType = [string]$records.data[0].checkinType
$duplicateEntryType = if ($originalEntryType -eq "QR") { "NFC" } else { "QR" }
$duplicateBody = @{entryToken=$entryToken;entryType=$duplicateEntryType;latitude=31.03;longitude=121.22}
try {
    Invoke-Api -Method Post -Path "/api/v1/check-ins" -Headers $headers -Body $duplicateBody | Out-Null
    throw "Cross-channel duplicate check-in unexpectedly succeeded"
} catch {
    if ($_.ErrorDetails.Message) { $duplicateCode = ($_.ErrorDetails.Message | ConvertFrom-Json).code }
}
if ($duplicateCode -ne "DUPLICATE_CHECKIN") { throw "Duplicate check-in error code verification failed: $duplicateCode" }

[pscustomobject]@{
    result="PASS"
    userId=$login.data.userId
    meetingCount=$meetings.data.Count
    checkinRecords=$records.data.Count
    pointsBalance=$balance.data.balance
    ledgerEntries=$ledger.data.Count
    reusedExistingCheckin=$reusedExistingCheckin
    crossChannelDuplicateCode=$duplicateCode
    traceId=$traceId
} | ConvertTo-Json -Compress
