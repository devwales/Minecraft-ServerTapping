const net = require('net');

// Configuration
const config = {
    host: 'localhost',
    port: 4567,
    password: 'abc123',
    command: 'boxgame tnt 5 4 hello'
};

// Create the request payload
const payload = {
    password: config.password,
    command: config.command
};

console.log('Connecting to server...');
console.log(`Host: ${config.host}`);
console.log(`Port: ${config.port}`);
console.log(`Command: ${config.command}\n`);

// Create client connection
const client = new net.Socket();

// Handle connection
client.connect(config.port, config.host, () => {
    console.log('Connected to server');
    
    // Send the command
    const jsonString = JSON.stringify(payload) + '\n';
    console.log('Sending payload:', jsonString);
    client.write(jsonString);
    console.log('Command sent');
});

// Handle data received
client.on('data', (data) => {
    console.log('Raw response:', data.toString());
    try {
        const response = JSON.parse(data.toString().trim());
        console.log('\nServer Response:');
        console.log('Success:', response.success);
        console.log('Message:', response.message);
    } catch (e) {
        console.error('Error parsing response:', e.message);
        console.error('Received data:', data.toString());
    }
    client.end();
});

// Handle connection errors
client.on('error', (err) => {
    console.error('\nConnection error:', err.message);
    process.exit(1);
});

// Handle connection close
client.on('close', () => {
    console.log('\nConnection closed');
}); 