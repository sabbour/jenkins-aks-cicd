from flask import Flask, request, render_template
import requests
import json
import os
import random
import socket
import sys
import logging

app = Flask(__name__)

# Load configurations
button1 =       "Cats" 
button2 =       "Dogs"
title =         "Azure Vote App"

# Service endpoint
api_url_base = 'http://' + os.environ['BACKEND']

# Change title to host name to demo NLB
title = title + " " + socket.gethostname()

@app.route('/', methods=['GET', 'POST'])
def index():

    # Vote tracking
    vote1 = 0
    vote2 = 0

    if request.method == 'GET':

        logging.warning('GET')

        # Get current values
        # Call the backend api
        api_url = '{0}/votes'.format(api_url_base)
        headers = {'Content-Type': 'application/json'}
        response = requests.get(api_url, headers=headers)
        response_json = json.loads(response.text)

        # Parse results
        for i in response_json:
            if i["option"] == button1:
                vote1 = i["count"]
            elif i["option"] == button2:
                vote2 = i["count"]              

        # Return index with values
        return render_template("index.html", value1=vote1, value2=vote2, button1=button1, button2=button2, title=title)

    elif request.method == 'POST':

        if request.form['vote'] == 'reset':
            
            # Call the backend api
            api_url = '{0}/reset'.format(api_url_base)
            response = requests.post(api_url)
            
            return render_template("index.html", value1=vote1, value2=vote2, button1=button1, button2=button2, title=title)
        else:

            # Insert vote result into DB
            vote = request.form['vote']

            # Call the backend API
            api_url = '{0}/votes'.format(api_url_base)
            headers = {'Content-Type': 'text/plain'}
            response = requests.post(api_url,headers=headers,data=vote)
            
            # Get current values
            # Call the backend API
            api_url = '{0}/votes'.format(api_url_base)
            headers = {'Content-Type': 'application/json'}
            response = requests.get(api_url, headers=headers)
            response_json = json.loads(response.text)

            # Parse results
            for i in response_json:
                if i["option"] == button1:
                    vote1 = i["count"]
                elif i["option"] == button2:
                    vote2 = i["count"]        
                
            # Return results
            return render_template("index.html", value1=vote1, value2=vote2, button1=button1, button2=button2, title=title)

if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True, port=8080)