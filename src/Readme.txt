Name: Zain Umerani

Assignment: Programming Assignment One - Web Server

Date: 2/21/2018

Description: This is a web server that takes HTTP requests and returns an appropriate response. Once a connection is made and a request is accepted, a new thread is 
created to handle that request. There are many types of files that this web server can handle:
	-text/plain
	-text/css, text/javascript
	-images (jpeg and png)

Workflow: A socket is created and is listening on a port (passed in through command line). Requests come in and are parsed - the file is extracted and the server makes 
sure that it exists and that it is readable. If it is, then a 200 response is sent back along with the contents of the file. If not, either a 403 (cannot access) code 
is sent, or a 404 (not found) is sent. Codes that are handled: 200, 404, 403.

Submitted Files:
	1. index.html
	2. WebServer.java
	3. files/ (all the images and CSS/JS that index.html depends on).

Instructions: 
	1. chmod u+x script.sh
	2. ./script <document_root> <port_number>

 
