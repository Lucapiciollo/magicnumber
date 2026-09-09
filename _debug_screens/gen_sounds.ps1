param(
    [string]$OutDir = "app\src\main\res\raw"
)

function New-Sample {
    param($t, $notes)
    $v = 0.0
    foreach ($n in $notes) {
        $rel = $t - $n.Start
        if ($rel -ge 0 -and $rel -lt $n.Dur) {
            $attack = 0.008
            if ($rel -lt $attack) {
                $env = $rel / $attack
            } else {
                $env = [Math]::Exp(-($rel - $attack) * $n.Decay)
            }
            $phase = 2 * [Math]::PI * $n.Freq * $rel
            # bell-like timbre: fundamental + soft overtone, slight vibrato shimmer
            $shimmer = 1.0 + 0.06 * [Math]::Sin(2 * [Math]::PI * 6 * $rel)
            $tone = [Math]::Sin($phase) + 0.30 * [Math]::Sin(2 * $phase) + 0.12 * [Math]::Sin(3 * $phase)
            $v += $n.Amp * $env * $tone * $shimmer
        }
    }
    return $v
}

function Write-Wav {
    param(
        [string]$Path,
        [double]$DurationSec,
        [array]$Notes,
        [int]$SampleRate = 44100
    )
    $numSamples = [int]($DurationSec * $SampleRate)
    $samples = New-Object double[] $numSamples
    for ($i = 0; $i -lt $numSamples; $i++) {
        $t = $i / $SampleRate
        $samples[$i] = New-Sample -t $t -notes $Notes
    }

    $peak = 0.0001
    for ($i = 0; $i -lt $numSamples; $i++) {
        $a = [Math]::Abs($samples[$i])
        if ($a -gt $peak) { $peak = $a }
    }
    $scale = 0.82 / $peak

    $bytesData = New-Object byte[] ($numSamples * 2)
    for ($i = 0; $i -lt $numSamples; $i++) {
        $s = $samples[$i] * $scale
        if ($s -gt 1.0) { $s = 1.0 }
        if ($s -lt -1.0) { $s = -1.0 }
        $val = [int16]([Math]::Round($s * 32767))
        $bytes = [BitConverter]::GetBytes($val)
        $bytesData[$i * 2] = $bytes[0]
        $bytesData[$i * 2 + 1] = $bytes[1]
    }

    $byteRate = $SampleRate * 2
    $dataSize = $bytesData.Length
    $riffSize = 36 + $dataSize

    $fs = [System.IO.File]::Open($Path, [System.IO.FileMode]::Create)
    $bw = New-Object System.IO.BinaryWriter($fs)
    $bw.Write([System.Text.Encoding]::ASCII.GetBytes("RIFF"))
    $bw.Write([int]$riffSize)
    $bw.Write([System.Text.Encoding]::ASCII.GetBytes("WAVE"))
    $bw.Write([System.Text.Encoding]::ASCII.GetBytes("fmt "))
    $bw.Write([int]16)
    $bw.Write([int16]1)
    $bw.Write([int16]1)
    $bw.Write([int]$SampleRate)
    $bw.Write([int]$byteRate)
    $bw.Write([int16]2)
    $bw.Write([int16]16)
    $bw.Write([System.Text.Encoding]::ASCII.GetBytes("data"))
    $bw.Write([int]$dataSize)
    $bw.Write($bytesData)
    $bw.Flush()
    $bw.Close()
    $fs.Close()
    Write-Host "Wrote $Path ($numSamples samples, $([Math]::Round($DurationSec,3))s)"
}

# --- 1) Button tap: short bright magic tick ---
$tapNotes = @(
    @{ Freq = 1500; Start = 0.0; Dur = 0.09; Amp = 0.5; Decay = 34 }
)
Write-Wav -Path (Join-Path $OutDir "sfx_tap.wav") -DurationSec 0.10 -Notes $tapNotes

# --- 2) Confirm: two-note ascending chime ---
$confirmNotes = @(
    @{ Freq = 880;  Start = 0.00; Dur = 0.14; Amp = 0.5; Decay = 20 }
    @{ Freq = 1318.51; Start = 0.08; Dur = 0.18; Amp = 0.5; Decay = 16 }
)
Write-Wav -Path (Join-Path $OutDir "sfx_confirm.wav") -DurationSec 0.30 -Notes $confirmNotes

# --- 3) Generate tick: soft twinkle used while numbers rotate ---
$tickNotes = @(
    @{ Freq = 1760; Start = 0.0; Dur = 0.07; Amp = 0.38; Decay = 42 }
)
Write-Wav -Path (Join-Path $OutDir "sfx_generate_tick.wav") -DurationSec 0.08 -Notes $tickNotes

# --- 4) Reveal: rewarding ascending arpeggio (C5 E5 G5 C6) ---
$revealNotes = @(
    @{ Freq = 523.25;  Start = 0.00; Dur = 0.28; Amp = 0.45; Decay = 9 }
    @{ Freq = 659.25;  Start = 0.09; Dur = 0.30; Amp = 0.45; Decay = 8 }
    @{ Freq = 783.99;  Start = 0.18; Dur = 0.32; Amp = 0.45; Decay = 7 }
    @{ Freq = 1046.50; Start = 0.27; Dur = 0.42; Amp = 0.50; Decay = 5 }
)
Write-Wav -Path (Join-Path $OutDir "sfx_reveal.wav") -DurationSec 0.75 -Notes $revealNotes

Write-Host "Done."
