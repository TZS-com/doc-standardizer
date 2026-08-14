param(
    [Parameter(Mandatory = $true)]
    [string]$TemplateFile,
    [string]$Maven = "mvn"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$distribution = Join-Path $projectRoot "dist\doc-standardizer"
$archive = Join-Path $projectRoot "dist\doc-standardizer-windows.zip"
$jar = Join-Path $projectRoot "doc-standardizer-task\target\doc-standardizer-task-1.0.0.jar"

if (-not (Test-Path -LiteralPath $TemplateFile -PathType Leaf)) { throw "Template file does not exist: $TemplateFile" }

& $Maven -f (Join-Path $projectRoot "pom.xml") -pl doc-standardizer-task -am -DskipTests package
if ($LASTEXITCODE -ne 0) { throw "Maven package failed." }
if (-not (Get-Command jlink -ErrorAction SilentlyContinue)) { throw "A JDK with jlink is required to create the Windows package." }

Remove-Item -Recurse -Force -LiteralPath $distribution -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $distribution, (Join-Path $distribution "input"), (Join-Path $distribution "output") | Out-Null
Copy-Item -LiteralPath $jar -Destination (Join-Path $distribution "doc-standardizer.jar")
Copy-Item -LiteralPath $TemplateFile -Destination (Join-Path $distribution "template.docx")
@'
@echo off
setlocal
cd /d "%~dp0"
"%~dp0runtime\bin\java.exe" -jar "%~dp0doc-standardizer.jar" %*
set "exitCode=%ERRORLEVEL%"
if not "%exitCode%"=="0" pause
exit /b %exitCode%
'@ | Set-Content -Encoding ASCII (Join-Path $distribution "doc-standardizer.bat")
@'
1. Put the files to be standardized into the input folder.
2. Replace template.docx with the approved company template.
3. Double-click doc-standardizer.bat.
4. Find the standardized files in the output folder. Source files are not changed.
'@ | Set-Content -Encoding UTF8 (Join-Path $distribution "README.txt")

& jlink --add-modules java.se --strip-debug --no-header-files --no-man-pages --compress=2 --output (Join-Path $distribution "runtime")
if ($LASTEXITCODE -ne 0) { throw "jlink runtime creation failed." }
Remove-Item -Force -LiteralPath $archive -ErrorAction SilentlyContinue
Compress-Archive -Path $distribution -DestinationPath $archive
Write-Host "Windows distribution created: $archive"
