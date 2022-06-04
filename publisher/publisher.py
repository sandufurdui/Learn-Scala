import socket
import requests
import re
import http.client as lol
import random
from time import sleep


IP = "localhost"
PORT = 5600
n = 0

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

client_socket.connect((IP, PORT))

print("connected to " + IP + " on port " + str(PORT))

conn = lol.HTTPConnection("localhost:4000")
conn.request("GET", "/tweets/1")
response = conn.getresponse()

print("sending data")

while True:
    data = response.readline()
    sleep_a_bit = random.randrange(0, 10) / 10
    # print(data_json)

    # sleep(sleep_a_bit)
    data_decoded = data.decode("utf-8")

    first_word = re.split("\s", data_decoded)[0]
    
    if first_word == 'data:':
        # print(data_decoded.split(' ', 1)[1])
        to_send = data_decoded.split(' ', 1)[1]
        print("sent " + str(n) + " messages")
        n = n + 1
        client_socket.send(to_send.encode())
