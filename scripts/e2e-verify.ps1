$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api/v1'
$stamp = Get-Date -Format 'HHmmss'
$userA = "e2e_a_$stamp"
$userB = "e2e_b_$stamp"
$password = 'TestPass123!'
$passed = [System.Collections.Generic.List[string]]::new()

function Assert-True([bool]$condition, [string]$message) {
  if (-not $condition) { throw "ASSERT FAILED: $message" }
  $script:passed.Add($message)
}

function Invoke-Api([string]$method, [string]$path, $body = $null, [string]$token = '', [string]$accept = 'application/json') {
  $headers = @{ Accept = $accept }
  if ($token) { $headers['Authorization'] = "Bearer $token" }
  $params = @{
    Method = $method
    Uri = "$base$path"
    Headers = $headers
    SkipHttpErrorCheck = $true
  }
  if ($null -ne $body) {
    $params.ContentType = 'application/json'
    $params.Body = ($body | ConvertTo-Json -Depth 12 -Compress)
  }
  $response = Invoke-WebRequest @params
  $status = [int]$response.StatusCode
  $content = $response.Content
  return [pscustomobject]@{ Status = $status; Content = $content; Headers = $response.Headers }
}

function Json([string]$method, [string]$path, $body = $null, [string]$token = '') {
  $r = Invoke-Api $method $path $body $token
  $parsed = if ([string]::IsNullOrWhiteSpace($r.Content)) { $null } else { $r.Content | ConvertFrom-Json }
  return [pscustomobject]@{ Status = $r.Status; Body = $parsed; Raw = $r.Content; Headers = $r.Headers }
}

function Assert-ApiOk($result, [string]$label) {
  Assert-True ($result.Status -eq 200) "$label returns HTTP 200 (actual $($result.Status))"
  Assert-True ($null -ne $result.Body) "$label returns a JSON body"
  Assert-True ($result.Body.code -eq 0) "$label returns business code 0"
  return $result.Body.data
}

Write-Output "=== E2E users: $userA / $userB ==="

# Register and login two users
$regA = Assert-ApiOk (Json 'POST' '/auth/register' @{ username=$userA; password=$password; nickname='E2E-A' }) 'register user A'
$regB = Assert-ApiOk (Json 'POST' '/auth/register' @{ username=$userB; password=$password; nickname='E2E-B' }) 'register user B'
Assert-True ([bool]$regA.token) 'user A receives access token'
Assert-True ([bool]$regA.refreshToken) 'user A receives refresh token'

$loginA = Assert-ApiOk (Json 'POST' '/auth/login' @{ username=$userA; password=$password }) 'login user A'
$tokenA = $loginA.token
$refreshA = $loginA.refreshToken
Assert-True ([bool]$tokenA) 'user A login returns access token'

$refreshResult = Assert-ApiOk (Json 'POST' '/auth/refresh' @{ refreshToken=$refreshA }) 'refresh access token'
Assert-True ([bool]$refreshResult.token) 'refresh returns a new access token'
$tokenA = $refreshResult.token
$refreshA = $refreshResult.refreshToken

$meA = Assert-ApiOk (Json 'GET' '/auth/me' $null $tokenA) 'get current user'
Assert-True ($meA.username -eq $userA) 'current user matches logged-in user'

$loginB = Assert-ApiOk (Json 'POST' '/auth/login' @{ username=$userB; password=$password }) 'login user B'
$tokenB = $loginB.token

function Complete-Assessment([string]$token, [string]$label) {
  $attempt = Assert-ApiOk (Json 'POST' '/attempts' $null $token) "$label start attempt"
  Assert-True ($attempt.id -gt 0) "$label receives attempt id"

  $questions = Assert-ApiOk (Json 'GET' "/attempts/$($attempt.id)/questions" $null $token) "$label fetch questions"
  Assert-True ($questions.Count -eq 36) "$label receives 36 questions (actual $($questions.Count))"

  $answers = @()
  foreach ($q in $questions) {
    $answers += @{ questionId = $q.id; answer = if ($q.sortOrder % 2 -eq 0) { 'A' } else { 'B' } }
  }
  $null = Assert-ApiOk (Json 'POST' "/attempts/$($attempt.id)/answers" @{ answers=$answers } $token) "$label submit answers"
  $result = Assert-ApiOk (Json 'POST' "/attempts/$($attempt.id)/complete" $null $token) "$label complete assessment"
  Assert-True ($result.typeCode.Length -eq 4) "$label produces a 4-letter personality type"
  return $result
}

$resultA = Complete-Assessment $tokenA 'user A'
$resultB = Complete-Assessment $tokenB 'user B'

$myResults = Assert-ApiOk (Json 'GET' '/results/my?page=0&size=10' $null $tokenA) 'user A result history'
Assert-True ($myResults.totalElements -ge 1) 'user A result history contains the completed assessment'

$growth = Assert-ApiOk (Json 'GET' '/user/growth?limit=20' $null $tokenA) 'growth track'
Assert-True ($growth.Count -ge 1) 'growth track contains assessment points'

$career = Assert-ApiOk (Json 'GET' '/user/career-advice' $null $tokenA) 'career advice'
Assert-True ([bool]$career.typeCode) 'career advice includes personality type'
Assert-True ($career.recommendedRoles.Count -ge 1) 'career advice includes recommended roles'

$compat = Assert-ApiOk (Json 'POST' '/user/compatibility' @{ otherUsername=$userB } $tokenA) 'compatibility'
Assert-True ($compat.score -ge 0) 'compatibility includes a score'

$exportFile = [System.IO.Path]::GetTempFileName()
try {
  $export = Invoke-WebRequest -Method GET -Uri "$base/user/export" -Headers @{ Authorization = "Bearer $tokenA"; Accept = 'text/csv' } -OutFile $exportFile -PassThru -SkipHttpErrorCheck
  $bytes = [System.IO.File]::ReadAllBytes($exportFile)
  $csvText = [System.Text.Encoding]::UTF8.GetString($bytes)
  Assert-True ([int]$export.StatusCode -eq 200) "CSV export returns HTTP 200 (actual $([int]$export.StatusCode))"
  Assert-True ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) 'CSV export starts with UTF-8 BOM'
  Assert-True ($csvText -match '记录ID,测试时间,性格类型') 'CSV export contains the expected header'
  Assert-True ($csvText -match $resultA.typeCode) 'CSV export contains the completed assessment record'
} finally {
  Remove-Item -LiteralPath $exportFile -Force -ErrorAction SilentlyContinue
}

# Admin flow
$adminLogin = Assert-ApiOk (Json 'POST' '/auth/login' @{ username='admin'; password='admin123' }) 'admin login'
$adminToken = $adminLogin.token
Assert-True ($adminLogin.user.role -eq 'ADMIN') 'admin login returns ADMIN role'

$stats = Assert-ApiOk (Json 'GET' '/admin/statistics' $null $adminToken) 'admin statistics'
Assert-True ($stats.totalUsers -ge 2) 'admin statistics includes users'
Assert-True ($stats.totalAttempts -ge 2) 'admin statistics includes attempts'

$users = Assert-ApiOk (Json 'GET' '/admin/users?page=0&size=10' $null $adminToken) 'admin user list'
Assert-True ($users.totalElements -ge 2) 'admin user list returns users'

$dist = Assert-ApiOk (Json 'GET' '/admin/analytics/personality-distribution' $null $adminToken) 'personality distribution analytics'
Assert-True ($dist.Count -ge 1) 'personality distribution has data'

$completion = Assert-ApiOk (Json 'GET' '/admin/analytics/completion-rate?days=14' $null $adminToken) 'completion-rate analytics'
Assert-True ($completion.Count -ge 1) 'completion-rate analytics has data'

$questionList = Assert-ApiOk (Json 'GET' '/admin/questions' $null $adminToken) 'admin question list'
Assert-True ($questionList.Count -ge 36) 'admin question list exposes seeded questions'
$newQuestion = Assert-ApiOk (Json 'POST' '/admin/questions' @{
  dimensionId = 1
  content = "E2E temporary question $stamp"
  optionA = 'Option A'
  optionB = 'Option B'
  answerType = 'E'
  sortOrder = 999
} $adminToken) 'admin create question'
Assert-True ($newQuestion.id -gt 0) 'admin create question returns id'
$updatedQuestion = Assert-ApiOk (Json 'PUT' "/admin/questions/$($newQuestion.id)" @{
  dimensionId = 1
  content = "E2E updated question $stamp"
  optionA = 'Option A2'
  optionB = 'Option B2'
  answerType = 'I'
  sortOrder = 999
} $adminToken) 'admin update question'
Assert-True ($updatedQuestion.content -match 'updated') 'admin update question persists changes'
$null = Assert-ApiOk (Json 'DELETE' "/admin/questions/$($newQuestion.id)" $null $adminToken) 'admin delete question'

$personalities = Assert-ApiOk (Json 'GET' '/admin/personalities' $null $adminToken) 'admin personality list'
Assert-True ($personalities.Count -eq 16) 'admin personality list contains 16 types'
$typeCode = $resultA.typeCode
$currentPersonality = $personalities | Where-Object { $_.typeCode -eq $typeCode } | Select-Object -First 1
$updatedPersonality = Assert-ApiOk (Json 'PUT' "/admin/personalities/$typeCode" @{
  typeCode = $typeCode
  typeName = $currentPersonality.typeName
  description = $currentPersonality.description
  strengths = $currentPersonality.strengths
  weaknesses = $currentPersonality.weaknesses
  careerSuggestions = $currentPersonality.careerSuggestions
} $adminToken) 'admin update personality'
Assert-True ($updatedPersonality.typeCode -eq $typeCode) 'admin personality update persists'

# AI capability flow (mock provider is deterministic and keeps this suite offline)
$aiStatus = Assert-ApiOk (Json 'GET' '/ai/status' $null $tokenA) 'AI status'
Assert-True ($aiStatus.enabled -eq $true) 'AI capability is enabled'
Assert-True ([bool]$aiStatus.activeProvider) 'AI status exposes active provider'

$chat = Assert-ApiOk (Json 'POST' '/ai/chat' @{ sessionId=$null; message='请结合我的 MBTI 类型给出职业行动建议' } $tokenA) 'AI chat'
Assert-True ($chat.sessionId -gt 0) 'AI chat returns a session id'
Assert-True ([bool]$chat.reply) 'AI chat returns a non-empty reply'

$chatSession = Assert-ApiOk (Json 'GET' "/ai/sessions/$($chat.sessionId)" $null $tokenA) 'AI session detail'
Assert-True ($chatSession.messages.Count -eq 2) 'AI session stores the user and assistant exchange'
Assert-True ($chatSession.messages[0].role -eq 'USER') 'AI session first message is from the user'
Assert-True ($chatSession.messages[1].role -eq 'ASSISTANT') 'AI session second message is from the assistant'

$continuedChat = Assert-ApiOk (Json 'POST' '/ai/chat' @{ sessionId=$chat.sessionId; message='继续给出两周执行计划' } $tokenA) 'AI continued chat'
Assert-True ($continuedChat.sessionId -eq $chat.sessionId) 'AI continued chat reuses the session'

$aiSessions = Assert-ApiOk (Json 'GET' '/ai/sessions' $null $tokenA) 'AI session list'
Assert-True ($aiSessions.Count -ge 1) 'AI session list contains the conversation'

$ssePayloadFile = [System.IO.Path]::GetTempFileName()
try {
  [System.IO.File]::WriteAllText($ssePayloadFile, '{"sessionId":null,"message":"请流式分析求职优势"}', [System.Text.UTF8Encoding]::new($false))
  $sseLines = & curl.exe -sS -N --max-time 30 -X POST "$base/ai/chat/stream" `
    -H "Authorization: Bearer $tokenA" -H 'Accept: text/event-stream' -H 'Content-Type: application/json' `
    --data-binary "@$ssePayloadFile"
  $sseText = $sseLines -join "`n"
  Assert-True ([bool]$sseText) 'AI SSE returns a non-empty event stream'
  Assert-True ($sseText -match 'event:delta|event: delta') 'AI SSE emits delta events'
  Assert-True ($sseText -match 'event:done|event: done') 'AI SSE emits a done event'
} finally {
  Remove-Item -LiteralPath $ssePayloadFile -Force -ErrorAction SilentlyContinue
}

$health = Json 'GET' '/health'
Assert-True ($health.Status -eq 200 -and $health.Body.code -eq 0) 'public health endpoint is reachable'

Write-Output ""
Write-Output "=== PASSED: $($passed.Count) assertions ==="
$passed | ForEach-Object { Write-Output "  OK  $_" }
