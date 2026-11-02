$baseDir = "C:\Users\angel\OneDrive - Universidad Nacional de Costa Rica\Escritorio\programacion proyecto final\Proyecto-1-progra\Sistema de Punto de Venta"
$javac = "C:\Program Files\Java\jdk-25.0.4\bin\javac.exe"
$outDir = Join-Path $baseDir "out"
$srcDir = Join-Path $baseDir "src"
$argfile = Join-Path $baseDir "sources_local.txt"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
Get-ChildItem -Path $srcDir -Recurse -Filter *.java | ForEach-Object {
    "src\" + $_.FullName.Substring($srcDir.Length + 1)
} | Set-Content -Path $argfile -Encoding ASCII
& $javac -cp "$outDir;$(Join-Path $baseDir 'lib\mysql-connector-j-9.4.0.jar')" -d $outDir -sourcepath $srcDir "@$argfile"
$code = $LASTEXITCODE
Remove-Item -Force $argfile -ErrorAction SilentlyContinue
if ($code -eq 0) {
    Write-Host "COMPILACION EXITOSA"
} else {
    Write-Host "ERROR DE COMPILACION"
}
