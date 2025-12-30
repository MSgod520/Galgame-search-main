# setup_and_run.ps1

# Ensure correct working directory
Set-Location $PSScriptRoot

# 1. Configure JDK 21
$JDK_PATH = "C:\Java\jdk-25"
if (Test-Path $JDK_PATH) {
    Write-Host "Found JDK 25 at $JDK_PATH" -ForegroundColor Green
    $env:JAVA_HOME = $JDK_PATH
    $env:PATH = "$JDK_PATH\bin;$env:PATH"
}
else {
    Write-Error "JDK 25 not found at $JDK_PATH. Please check your installation."
    exit 1
}

# Verify Java
java -version

# 2. Check or Install Maven
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Write-Host "Maven is already installed." -ForegroundColor Green
    mvn javafx:run
}
else {
    Write-Host "Maven not found. Setting up portable Maven..." -ForegroundColor Yellow
    
    $MVN_DIR = Join-Path $PSScriptRoot ".mvn_dist"
    $MVN_ZIP = Join-Path $PSScriptRoot "maven.zip"
    $MVN_Home = Join-Path $MVN_DIR "apache-maven-3.9.6"
    $MVN_Bin = Join-Path $MVN_Home "bin"
    $MVN_Exec = Join-Path $MVN_Bin "mvn.cmd"

    # Check setup
    if (-not (Test-Path $MVN_Exec)) {
        # Create dir
        if (-not (Test-Path $MVN_DIR)) { New-Item -ItemType Directory -Path $MVN_DIR | Out-Null }
        
        # Download
        if (-not (Test-Path $MVN_ZIP)) {
            Write-Host "Downloading Maven (3.9.6) from Archive..."
            $Url = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
            try {
                Invoke-WebRequest -Uri $Url -OutFile $MVN_ZIP -ErrorAction Stop
            }
            catch {
                Write-Error "Failed to download Maven. Check internet connection."
                Remove-Item $MVN_ZIP -ErrorAction SilentlyContinue
                exit 1
            }
        }
        
        # Extract
        if (Test-Path $MVN_ZIP) {
            Write-Host "Extracting Maven..."
            Expand-Archive -Path $MVN_ZIP -DestinationPath $MVN_DIR -Force
        }
    }

    # Run
    if (Test-Path $MVN_Exec) {
        Write-Host "Starting Application with Portable Maven..." -ForegroundColor Green
        & $MVN_Exec javafx:run
    }
    else {
        Write-Error "Maven executable not found at $MVN_Exec. Setup failed."
        exit 1
    }
}
