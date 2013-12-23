#!/usr/bin/env python

import httplib2 as http
import json
from urllib import urlencode
import sys

try:
    from urlparse import urlparse
except ImportError:
    from urllib.parse import urlparse

uri = 'http://192.168.33.10:8080'

def status(id):
    new_path = '/status/'

    target = urlparse(uri + new_path + str(id))
    method = 'GET'

    h = http.Http()

    response, content = h.request(
            target.geturl(),
            method,
            "")

    if response.get('status') == '200':
        data = json.loads(content)
        print data
    else:
        print response.get('status')
        print content

def new(url):
    new_path = '/new'
    headers = {"Content-type": "application/x-www-form-urlencoded",
               "Accept": "text/plain"}

    target = urlparse(uri + new_path)
    method = 'POST'
    data = {'url': url}

    h = http.Http()

    response, content = h.request(
            target.geturl(),
            method,
            urlencode(data),
            headers)

    if response.get('status') == '200':
        data = json.loads(content)
        print data.get('id')
    else:
        print response.get('status')
        print content

def retrieve(url):
    import subprocess
    p = subprocess.Popen(['axel', url])
    sys.exit(p.wait())

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print "Usage: jsonClient.py [new|status|retrieve] [id|url]"
    else:
        if sys.argv[1] == 'retrieve':
            retrieve(sys.argv[2])
        elif sys.argv[1] == 'new':
            new(sys.argv[2])
        elif sys.argv[1] == 'status':
            status(int(sys.argv[2]))
        else:
            print "Usage: jsonClient.py method [id|url]"
