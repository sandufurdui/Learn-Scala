import socket
import select
import errno


IP = "localhost"
PORT = 5601

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect to a given ip and port
client_socket.connect((IP, PORT))
while True:
    print("listen for connections")
    print(client_socket.recv().decode())