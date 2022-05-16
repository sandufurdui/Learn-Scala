import socket
import requests
import re
import http.client as lol
import random
from time import sleep


s = socket.socket()
print ("Socket successfully created")

port = 5600

s.bind(('localhost', port))

s.listen(5)
print ("socket is redy to send")

c, addr = s.accept()
print ('Got connection from', addr )


conn = lol.HTTPConnection("localhost:4000")
conn.request("GET", "/tweets/1")
response = conn.getresponse()

while True:
    data = response.readline()
    sleep_a_bit = random.randrange(0, 10) / 10
    # print(data_json)
    sleep(sleep_a_bit)
    data_decoded = data.decode("utf-8")

    first_word = re.split("\s", data_decoded)[0]

    if first_word == 'data:':
        # print(first_word)
        c.send(data_decoded.encode())
