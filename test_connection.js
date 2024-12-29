const net = require('net');

// Configuration
const config = {
    host: 'localhost',
    port: 4567,
    command: '__test__'  // Special test command
};

// Create the request payload
const payload = {
    password: '',  // Not needed for test
    command: config.command
};

console.log('Testing connection to Minecraft server...');
console.log(`Host: ${config.host}`);
console.log(`Port: ${config.port}\n`);

// Create client connection
const client = new net.Socket();

// Set a timeout of 5 seconds
const timeout = setTimeout(() => {
    console.error('Connection timed out');
    client.destroy();
    process.exit(1);
}, 5000);

// Handle connection
client.connect(config.port, config.host, () => {
    console.log('Connected to server');
    
    // Send the test command
    const jsonString = JSON.stringify(payload) + '\n';
    client.write(jsonString);
});

// Handle data received
client.on('data', (data) => {
    clearTimeout(timeout);
    try {
        const response = JSON.parse(data.toString().trim());
        console.log('\nServer Information:');
        console.log('Connected:', response.connected);
        console.log('Server Version:', response.serverVersion);
        console.log('Plugin Version:', response.pluginVersion);
        console.log('Server Time:', new Date(response.timestamp).toLocaleString());
    } catch (e) {
        console.error('Error parsing response:', e.message);
        console.error('Received data:', data.toString());
    }
    client.end();
});

// Handle connection errors
client.on('error', (err) => {
    clearTimeout(timeout);
    console.error('\nConnection error:', err.message);
    process.exit(1);
});

// Handle connection close
client.on('close', () => {
    clearTimeout(timeout);
    console.log('\nConnection closed');
}); 