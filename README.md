Here’s a tidied-up version of your `README.md`:

```markdown
# Connector Plugin

A secure Minecraft Paper plugin that allows remote command execution through a TCP socket connection.

## Features

- Secure password authentication
- IP-based rate limiting
- Temporary IP bans for failed authentication attempts
- Configurable port and password
- JSON-based communication
- In-game reload command
- Connection testing endpoint

## Installation

1. Download the latest release or build from source.
2. Place the JAR file in your server's `plugins` folder.
3. Start/restart your server.
4. Configure the plugin in `plugins/Connector/config.yml`.

## Building from Source

```bash
git clone https://your-repository/connector.git
cd connector
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Configuration

The plugin creates a `config.yml` file in `plugins/Connector/` with the following options:

```yaml
# Port for the remote command server
port: 25566

# Password for authentication (change this!)
password: "your_secure_password_here"

# Maximum number of failed authentication attempts before temporary IP ban
max_failed_attempts: 5

# Ban duration in minutes after exceeding max failed attempts
ban_duration: 30
```

## Usage

### In-Game Commands

- `/connector reload` - Reloads the configuration (requires `connector.admin` permission)

### Remote Command Execution

To execute commands remotely, send a JSON payload to the configured port. The JSON format is:

```json
{
  "password": "your_secure_password_here",
  "command": "say Hello World"
}
```

The server will respond with:

```json
{
  "success": true,
  "message": "Command executed"
}
```

### Example Usage with Different Tools

1. **Using netcat**:

```bash
echo '{"password":"your_secure_password_here","command":"say Hello World"}' | nc localhost 25566
```

2. **Using Python**:

```python
import socket
import json

def send_command(host, port, password, command):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    request = {
        "password": password,
        "command": command
    }
    sock.send((json.dumps(request) + "\n").encode())
    response = sock.recv(1024).decode()
    sock.close()
    return json.loads(response)

# Example usage
response = send_command("localhost", 25566, "your_secure_password_here", "say Hello World")
print(response)
```

3. **Using curl**:

```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"password":"your_secure_password_here","command":"say Hello World"}' \
localhost:25566
```

## Security Considerations

1. **Change the Default Password**: Immediately change the default password in `config.yml`.
2. **Firewall Configuration**:
   - Restrict access to the plugin's port.
   - Only allow connections from trusted IP addresses.
   - Consider using a VPN or SSH tunnel for remote access.
3. **Use Strong Passwords**: Ensure you use a strong, unique password.
4. **Regular Monitoring**:
   - Monitor server logs for failed authentication attempts.
   - Watch for unusual patterns of access.

## Troubleshooting

1. **Connection Refused**:
   - Verify the port is correctly configured.
   - Check if the port is blocked by a firewall.
   - Ensure the plugin is running (`/plugins list`).
   
2. **Authentication Failed**:
   - Verify the password in `config.yml`.
   - Check if the IP is temporarily banned.
   - Ensure JSON payload is correctly formatted.
   
3. **Commands Not Executing**:
   - Check the server console for errors.
   - Verify the command sender has appropriate permissions.
   - Ensure commands are properly formatted.

## Permissions

- `connector.admin` - Required for using the `/connector reload` command.

## Support

For issues, bug reports, or feature requests, please create an issue on the GitHub repository.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Connection Testing

The plugin provides a special test endpoint that allows you to verify the connection and get server information without authentication. This is useful for monitoring and integration purposes.

### Using the Test Script

1. Save the test script as `test_connection.js`
2. Run it using Node.js:

```bash
node test_connection.js
```

### Test Response Format

The test endpoint returns a JSON response with server information:

```json
{
  "connected": true,
  "serverVersion": "Paper version git-Paper-xxx (MC: 1.21)",
  "pluginVersion": "1.0",
  "timestamp": 1647789012345
}
```

### Custom Implementation

To implement the test in your own code, send this JSON payload:

```json
{
  "password": "",
  "command": "__test__"
}
```

Example using Node.js:
```javascript
const net = require('net');
const client = new net.Socket();

const payload = {
    password: '',
    command: '__test__'
};

client.connect(4567, 'localhost', () => {
    client.write(JSON.stringify(payload) + '\n');
});

client.on('data', (data) => {
    const response = JSON.parse(data.toString().trim());
    console.log('Server Status:', response);
    client.end();
});
```