`rm *.class`
`javac WebServer.java`
dr=$1
port=$2
echo "Running server on port ${port} ..."
`java WebServer -document_root ${dr} -port ${port}`
