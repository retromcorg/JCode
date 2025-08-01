# JCode

**JCode** is a Bukkit plugin that generates short, secure, time-based authentication codes for players.  
These codes can be used to authenticate players with external services such as cape or skin systems.

---

## Features
- Secure SHA-256 code generation
- Codes change every **5 minutes**
- Per-player & per-service codes
- Configurable secret key
- Permission-based access

---

## Installation
1. Place `JCode.jar` in your `plugins/` folder.
2. Start the server to create `config.yml`.
3. Edit `settings.key.value` with your own secure key.
4. Restart or reload the server.

---

## Command
`/jcode <service>` — Generates a code for the given service.  
Requires `jcode.code` and `jcode.code.<service>` permissions.

---

## How Codes Work
1. Determine current **5-minute epoch block**:
   ```
   currentEpochMinutes / 5
   ```
2. Create hash from:
   ```
   player-uuid : config-key : epoch-block : service
   ```
3. Take the first **6 characters** as the code.

---

### 🖥 PHP Verification Example
```php
<?php
function generateJCode($playerUuid, $configKey, $service) {
    $epochMinutes = floor(microtime(true) / 60);
    $block = floor($epochMinutes / 5);
    $codeString = $playerUuid . ":" . $configKey . ":" . $block . ":" . strtolower($service);
    $hash = hash("sha256", $codeString);
    return substr($hash, 0, 6);
}

$uuid = "123e4567-e89b-12d3-a456-426614174000";
$key = "your-secure-key";
$service = "cape";

echo "Generated code: " . generateJCode($uuid, $key, $service);
?>
```

---

## License
Proprietary to **RetroMC**. Unauthorized distribution or modification prohibited.