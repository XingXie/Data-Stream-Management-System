#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
//added by xing
#include <cstdlib>
// Xing: MLS raw data filtering is addressed
/// DEBUG
#include <iostream>

//DIS

#include <stdio.h>
#include <vector>
#include <sstream>
#include <ifaddrs.h>
//

using namespace std;

#define cp(x) cout << "Checkpoint: " << x << endl;

#ifndef _NET_MGR_
#include "net_mgr.h"
#endif

#ifndef _GEN_CONN_
#include "gen_conn.h"
#endif

#ifndef _DEBUG_
#include "debug.h"
#endif

#ifndef _NET_ERROR_
#include "net_error.h"
#endif

#ifndef _IN_CONN_
#include "in_conn.h"
#endif

#ifndef _OUT_CONN_
#include "out_conn.h"
#endif

static const int MAX_FILE_NAME = 128;
static char logFile [MAX_FILE_NAME];
//DIS
static const int MAX_NO_SERVERS = 20;
//...

using namespace Network;
using namespace std;

//DIS
vector<payload> NetworkManager::ip_table (0);
//

//SID Statistics Functions: Begin
#define NOT_FOUND -1
#define BROADCAST_IP 2

char loadtemp[10];
// Get system Load
char* NetworkManager::getLoad()  {
	FILE* fp;
	char write_buff[1440];
//	string write_buff;
	char path[100];
	int i = 0;
	/* Open the command for reading. */
	fp = popen("top -b -d1 -n2 | grep Cpu | cut -c 35-39", "r");
	if (fp == NULL) {
		printf("Failed to run command\n" );
		exit;
	}

	/* Read the output a line at a time - output it. */
	while (fgets(path, sizeof(path)-1, fp) != NULL) {
		if(i==1){
			strncpy(write_buff,path,sizeof(path));
		}
		i++;
	}
	/* close */
	pclose(fp);
//	return write_buff;
	std:string temptemp(write_buff);
		strcpy(loadtemp, temptemp.c_str());
//		printf("Value of loadtemp (CPU number):%s\n", loadtemp);
		return loadtemp;


}

// Utility function
int NetworkManager::strpos(char *haystack, char *needle){
   char *p = strstr(haystack, needle);
   if (p)
      return p - haystack;
   return NOT_FOUND;
}
char temp1[10];
char temptemp1[10];
// Returns CPU speed and cores
char * NetworkManager::getModel(int para){
	//printf("Point1");
	char *inname = "/proc/cpuinfo";
    FILE *infile;
    char line_buffer[BUFSIZ]; /* BUFSIZ is defined if you include stdio.h */
    int pos = NOT_FOUND;
    int processor_count=0;
    char model[100];
    char *pch;
    char ret[20];
    char str[10] = {"0"};
 //   std:string str1;
//    std::stringstream strout;

    char *a;
    infile = fopen(inname, "r");
    if (!infile) {
        printf("Couldn't open file %s for reading.\n", inname);
        return 0;
    }
    while (fgets(line_buffer, sizeof(line_buffer), infile)) {
        if ( (pos = strpos(line_buffer,"model name")) != NOT_FOUND ){
			processor_count++;
			strcpy(model,line_buffer);
		}
    }
  //strout << processor_count;
   // str = strout.str().c_str();
    sprintf(str, "%d", processor_count);
  //  cout << "processor_count: " << processor_count << endl;
 //   cout << "str after processor_count: " << str << endl;

    pch = strtok (model,"@");
    pch = strtok (NULL, "@");
    if(para==1){
		//printf("length of pch:%d\n%s", strlen(pch), pch);
		pch++;
		std::string temp(pch);
		temp.erase(temp.find_last_not_of(" \n\r\t")+1);

		//std::cout << "BEG" << temp <<"End"<< std::endl;
		strcpy(temp1, temp.c_str());
	//	printf("Value of temp1 (CPU GHZ):%s\n", temp1);
		//cout << temp1 << std::endl;
		return temp1;
	}else if(para==2){
		std:string temptemp(str);
		strcpy(temptemp1, temptemp.c_str());
//		printf("Value of temptemp1 (CPU number):%s\n", temptemp1);
		return temptemp1;
		//	printf("Value of str (CPU number):%s\n", str);

	//	return str;
	}
	else {
		printf("error para");
		exit(1);
	}
}
// Statistics Functions: End

// DIS additions: Begin
// For listening to incoming server connections
struct sockaddr_in localSock;
struct ip_mreq group;

// For broadcasting new servers

char databuf[1024];
int datalen = sizeof(databuf);
char addressBuffer[INET_ADDRSTRLEN];

// Payload Vars
std::vector<std::string> iptable;
char model[10];
char processors[10];
int global_port;
int global_serverLevel;

// Convert string to double
double NetworkManager::string_to_double( const std::string& s )
 {
   std::istringstream i(s);
   double x;
   if (!(i >> x))
     return 0;
   return x;
}
// Calculate the load thingy
int NetworkManager::calculate(string myString)  {
	string ip,token;
	double s,l,c;
    istringstream iss(myString);
    getline(iss, token, ',');
    getline(iss, token, ',');
    s = string_to_double(token);
    getline(iss, token, ',');
    c = string_to_double(token);
    getline(iss, token, ',');
    l = string_to_double(token);


//    cout << "Speed:" << s << " Cores:" << c << "Load:" << l;
    double f = s * c * ((l)/100);
    return f;
}

char* NetworkManager::getInterfaceAddress()  {
	struct ifaddrs * ifAddrStruct=NULL;
    struct ifaddrs * ifa=NULL;
    void * tmpAddrPtr=NULL;


    getifaddrs(&ifAddrStruct);

    for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) {
        if (ifa ->ifa_addr->sa_family==AF_INET) { // check it is IP4
            // is a valid IP4 Address
            tmpAddrPtr=&((struct sockaddr_in *)ifa->ifa_addr)->sin_addr;
            inet_ntop(AF_INET, tmpAddrPtr, addressBuffer, INET_ADDRSTRLEN);
        }
    }
    if (ifAddrStruct!=NULL) freeifaddrs(ifAddrStruct);
    return addressBuffer;
}

// Function to broadcast the server's IP address on the local subnet
void NetworkManager::broadcastIP()
{
	int fd;
	struct sockaddr_in localInterface = {0};
	/* Create a datagram socket on which to send. */
	fd = socket(PF_INET, SOCK_DGRAM, 0);
	if(fd < 0)
	{
		perror("Opening datagram socket error");
		exit(1);
	}
	else
	printf("Opening the datagram socket...OK.\n");



	//addr.imr_ifindex = if_nametoindex("eth1");
    if (setsockopt(fd, IPPROTO_IP, IP_MULTICAST_IF, (void*)&localInterface, sizeof(localInterface))<0){
		perror("setsockopt() failed");
		exit(1);
	}

    localInterface.sin_family = AF_INET;
    localInterface.sin_port = htons(5000);
    localInterface.sin_addr.s_addr = inet_addr("224.0.0.1");

    //inet_pton(AF_INET, "224.0.0.1", &localInterface.sin_addr);



	// Put the server IP in databuf
	//cout << "this->serverlevel: " << this->serverLevel << endl;
	bzero(&databuf, datalen);
	sprintf(databuf, "%s,%s,%s,%s",this->getInterfaceAddress(), this->getModel(1),this-> getModel(2),this-> getLoad());
	cout << "NetworkManager::broadcastIP(): databuf 1: " << databuf << endl;
	int val = calculate(databuf);
	cout << "NetworkManager::broadcastIP(): databuf after calculate: " << databuf << endl;
	bzero(&databuf, datalen);
	cout << "NetworkManager::broadcastIP(): databuf after bzero: " << databuf << endl;
	//xing: addd level info
	sprintf(databuf, "%s,%d,%d,%d",getInterfaceAddress(), global_port, val, global_serverLevel);
//	cout << "NetworkManager::broadcastIP(): global_port: " << global_port << endl;
	std::cout << "NetworkManager::broadcastIP(): databuf 2: " << databuf << std::endl;

	if (sendto(fd, databuf, sizeof(databuf), 0,(struct sockaddr*)&localInterface, sizeof(localInterface)) < 0) {
        perror("bugger");
        exit(0);
    }

	close(fd);

	//return 0;
}
string NetworkManager::getIP(string myString)  {
	//string myString = "129.89.76.9,5";

    string token;
    istringstream iss(myString);
    getline(iss, token, ',');
    //std::cout << token << std::endl;

    return token;
}
string last_ip;
// Function to listen to new servers
void* NetworkManager::getServerInfo()
{
	int sd;
	/***---***/
	struct sockaddr_in from_addr;
	unsigned int from_len;
	 int recv_len;

	/* Create a datagram socket on which to receive. */
	sd = socket(AF_INET, SOCK_DGRAM, 0);
	if(sd < 0)
	{
		perror("Opening datagram socket error");
		exit(1);
	}
	else
	printf("Opening datagram socket....OK.\n");

	/* Enable SO_REUSEADDR to allow multiple instances of this */
	/* application to receive copies of the multicast datagrams. */
	{
	int reuse = 1;
	if(setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, (char *)&reuse, sizeof(reuse)) < 0)
	{
		perror("Setting SO_REUSEADDR error");
		close(sd);
		exit(1);
	}
	else
		printf("Setting SO_REUSEADDR...OK.\n");
	}

	/* Bind to the proper port number with the IP address */
	/* specified as INADDR_ANY. */
	memset((char *) &localSock, 0, sizeof(localSock));
	localSock.sin_family = AF_INET;
	localSock.sin_port = htons(5000);
	localSock.sin_addr.s_addr = INADDR_ANY;
	if(bind(sd, (struct sockaddr*)&localSock, sizeof(localSock)))
	{
		perror("Binding datagram socket error");
		close(sd);
		exit(1);
	}
	else
		printf("Binding datagram socket...OK.\n");

	/* Join the multicast group 224.0.0.1 on the local  */
	/* interface. Note that this IP_ADD_MEMBERSHIP option must be */
	/* called for each local interface over which the multicast */
	/* datagrams are to be received. */
	group.imr_multiaddr.s_addr = inet_addr("224.0.0.1");
	group.imr_interface.s_addr = inet_addr(getInterfaceAddress());
	if(setsockopt(sd, IPPROTO_IP, IP_ADD_MEMBERSHIP, (char *)&group, sizeof(group)) < 0)
	{
		perror("Adding multicast group error");
		close(sd);
		exit(1);
	}
	else
		printf("Adding multicast group...OK.\n");

	/* Read from the socket. */
	datalen = sizeof(databuf);

	/***---***/

	char buf[1024];

	int j=0;
	cout << "before Read and iptable size: "  << ip_table.size() << endl;

	for(j = 0; j < ip_table.size(); j++)  {
std::cout << "ip before" << ip_table[j].ip << std::endl;
std::cout << "port before" << ip_table[j].port << std::endl;
std::cout << "value before" << ip_table[j].value << std::endl;
std::cout << "serverLevel before" << ip_table[j].serverLevel << std::endl;

	}

	for (;;) {

	/* clear the receive buffers & structs */
    memset(databuf, 0, sizeof(databuf));
    from_len = sizeof(from_addr);
    memset(&from_addr, 0, from_len);

    /* block waiting to receive a packet */
    if ((recv_len = recvfrom(sd, databuf, 1024, 0,
         (struct sockaddr*)&from_addr, &from_len)) < 0) {
      perror("recvfrom() failed");
      exit(1);
    }

    /* output received string */

		char strtokVar3[10][50]={""};
		char *strtokVar4;
		cout << "gettserverInfo: databuf: " << databuf << endl;
		strtokVar4 = strtok (databuf,",");
        int iIndex=0;
        while (strtokVar4 != NULL){
			strcpy(strtokVar3[iIndex],strtokVar4);
			strtokVar4 = strtok (NULL, ",");
            iIndex++;
        }
        char ip[20];
         char port[20];
          char value[20];
           char currIp[20];
         char currPort[20];
         // Xing level
         int sLevel = 0;
        strcpy(ip,strtokVar3[0]);
        strcpy(port,strtokVar3[1]);
        strcpy(value,strtokVar3[2]);
        sLevel = atoi(strtokVar3[3]);
 //       cout << "sLevel: " << sLevel << endl;

		bool check = false;
		int j =0;
		for(j = 0; j < ip_table.size(); j++)  {

			 strcpy(currIp,ip_table[j].ip.c_str());
			  strcpy(currPort,ip_table[j].port.c_str());

			if((strcmp(ip, currIp) == 0)  && (strcmp(port, currPort) == 0))   {
				cout << "ip & port found" << ip << endl;
				check = true;
				ip_table[j].value = atof(value);
				break;
			}
		}

		if(!check){
			if(sLevel != global_serverLevel){
				cout << "level not matching: do not add to my subnet with socket: " << port << endl;
			}
			else{
			payload pl;
			pl.ip = ip;
			pl.port = port;
			pl.value = atof(value);
			//Xing Level
			pl.serverLevel = sLevel;

			ip_table.push_back(pl);
			}

		}



		printf("\n\n");
		std::cout << "Size:" << ip_table.size() << std::endl;
		for(int i = 0; i < ip_table.size(); i++)  {

			std::cout << ip_table[i].ip <<" " << ip_table[i].port << " " << ip_table[i].value << std::endl;
		}
		broadcastIP();
		bzero(&databuf, sizeof(databuf));
		sleep(BROADCAST_IP);
	}
	close(sd);

	//return 0;
}
// ----Additions End
//DIS XING
NetworkManager::NetworkManager(int   portNo,
							   char *logFilePref,
							   char *configFile,
							   int serverLevel)
{
	this -> portNo      = portNo;
	this -> logFilePref = strdup (logFilePref);
	this -> configFile  = strdup (configFile);
	this -> serverId    = 0;
    this -> trustedRR  = new Execution::RoundRobinScheduler();
    //XING DIS
    this -> serverLevel = serverLevel;
	
	// Log for the network manager and all the connections
	sprintf (logFile, "%s", logFilePref);
	LOG.open (logFile, ofstream::app);
	
	// Initialize the thread pool
	for (int t = 0 ; t < NUM_THREADS ; t++)
		b_thread_in_use [t] = false;

	// Initialize input/output connections
	for (int i = 0 ; i < MAX_INPUT_CONN ; i++)
		input_conns [i] = 0;

	for (int o = 0 ; o < MAX_OUTPUT_CONN ; o++)
		output_conns [o] = 0;
	

	num_conns = 0;	

	//Xing: initialize the trustedRR

}

void NetworkManager::run()
{
	int sockfd, cli_sockfd, cli_len;
	struct sockaddr_in serv_addr, cli_addr;	
	
	LOG << "NetMgr: starting up" << endl;
	
	//-------------------------------------------------------------------
	// Open a socket, bind the address, and wait for new connections
	//-------------------------------------------------------------------
	
	if ((sockfd = socket (PF_INET, SOCK_STREAM, 0)) < 0) {
		LOG << "NetMgr: Can't open a stream socket" << endl;
		return;
	}
	
	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = PF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(portNo);
	
	if(bind(sockfd,
			(struct sockaddr *) &serv_addr,
			sizeof(serv_addr)) < 0) {
		LOG << "NetMgr: Can't bind to port " << portNo << endl;
		return;
	}
	
	//DIS
	LOG << "NetMgr: bind to port " << portNo << endl;

	broadcastIP();
	//------

	listen(sockfd, MAX_WAIT_CONN);
	
	while (true) {
		
		cli_len = sizeof(cli_addr);
		cli_sockfd = accept(sockfd, (struct sockaddr *) &cli_addr,							 
							(socklen_t *) &cli_len);
		
		if (cli_sockfd < 0) {
			LOG << "NetMgr: Error accepting a new connection" << endl;
			return;
		}
		
		if (handleNewConn (cli_sockfd) != 0) {
			LOG << "NetMgr: Error handling new connection" << endl;
			return;
		}
	}
	
	// never comes
	return;
}

// Start off a generic connection thread
int NetworkManager::handleNewConn (int cli_sockfd)
{
	int free_thread_id;
	int rc;
	int conn_id;
	GenericConnection *gen_conn;
	
	// Get a free thread to do the work for us
	free_thread_id = -1;
	for (int t = 0 ; t < (int) NUM_THREADS ; t++) {
		if (!b_thread_in_use [t]) {
			free_thread_id = t;
			break;
		}
	}
	
	// If we do not have a free thread, we just don't accept the
	// connection.  We write this situation to the log ...
	if (free_thread_id == -1) {
		
		LOG << "NetMgr: A request not handled due to lack of resources"
			<< endl;
		
		close (cli_sockfd);
		return 0;
	}
	
	// Create a new generic connection object
	ASSERT (num_conns <= MAX_CONN);
	
	// We do not have space to store connection related information, we
	// have to be rebooted :)
	if (num_conns == MAX_CONN) {
		LOG << "NetMgr: No space left on connection table" << endl;
		return -1;		
	}
	
	conn_id = num_conns;	
	// Xing: transfer the trusted RR info
	gen_conn = new GenericConnection (conn_id, this,									  
									  cli_sockfd, LOG);
	// Xing: check the cli_sockfd
	// cout << "ner_mgr.cc cli_sockfd: " << cli_sockfd << endl;

	connTable [conn_id].type = UNKNOWN;
	num_conns ++;
	
	// Start off the thread ...
	rc = pthread_create (&conn_threads [free_thread_id], NULL, &start_thread,
						 (void*)gen_conn);
	if (rc != 0) {
		LOG << "NetMgr: Error creating a new connection thread" << endl;
		return -1;
	}
	
	return 0;
}

char *NetworkManager::getLogFilePref () const {
	return logFilePref;
}

int NetworkManager::getNewServerId ()
{
	return serverId++;
}

char *NetworkManager::getConfigFile () const {
	return configFile;
}

int NetworkManager::registerCommandConn (int id) {
	ASSERT (id >= 0 && id < num_conns);
	
	connTable [id].type = COMMAND_CONN;
	return 0;
}
// Xing: MLS control is added.
int NetworkManager::createInputConn (int &conn_id, InputConnection *&conn, string userLevel)
{
	conn_id = -1;
	for (int i = 0 ; i < MAX_INPUT_CONN  ; i++) {		
		if (!input_conns [i]) {
			conn_id = i;
			break;
		}
	}
	
	// We don't have a free connection
	if (conn_id == -1) {
		return NET_MGR_RSRC_ERR;
	}

	// Create a new input connection
	//Xing: pass with user security level
	conn = new InputConnection (LOG, userLevel);

	input_conns [conn_id] = conn;
	return 0;
}

int NetworkManager::getInputConn (int conn_id, InputConnection *&conn)
{
	if (conn_id < 0 || conn_id > MAX_INPUT_CONN)
		return -1;

	if (!input_conns [conn_id])
		return -1;

	conn = input_conns [conn_id];

	return 0;
}

int NetworkManager::destroyInputConn (int conn_id)
{
	ASSERT (conn_id >= 0 && conn_id < MAX_INPUT_CONN);
	ASSERT (input_conns [conn_id]);

	LOG << "Destroying input connection: " << conn_id << endl;
	
	delete input_conns [conn_id];
	input_conns [conn_id] = 0;
	
	return 0;
}

int NetworkManager::createOutputConn (int &conn_id,
									  OutputConnection *&conn)
{
	conn_id = -1;
	
	for (int i = 0 ; i < MAX_OUTPUT_CONN ; i++) {
		if (!output_conns [i]) {
			conn_id = i;
			break;
		}
	}
	
	if (conn_id == -1) {
		return NET_MGR_RSRC_ERR;
	}
	
	conn = new OutputConnection (LOG);
	
	output_conns [conn_id] = conn;
	
	return 0;
}

int NetworkManager::getOutputConn (int conn_id, OutputConnection *&conn)
{
	if (conn_id < 0 || conn_id >= MAX_OUTPUT_CONN)
		return -1;

	if (!output_conns [conn_id])
		return -1;

	conn = output_conns [conn_id];

	return 0;
}

int NetworkManager::destroyOutputConn (int conn_id)
{
	ASSERT (conn_id >= 0 && conn_id < MAX_OUTPUT_CONN);
	ASSERT (output_conns [conn_id]);

	delete output_conns [conn_id];
	output_conns [conn_id] = 0;

	LOG << "Destroying output connection: " << conn_id << endl;
	
	return 0;
}

#include <iostream>
using namespace std;

// Option string
// XING DIS "l:c:p:" -> "l:c:p:x:"
static const char *opt_string = "l:c:p:x:";
// Xing: DIS: add level spec
static int getOpts (int argc,
					char *argv[],
					char *&logFilePref, char *&configFile,
					int &portNo,
					int &serverLevel)
{
	int c;
	
	logFilePref = configFile = 0;
	portNo = 0;
	// DIS Xing
	serverLevel = 0;
	while ((c = getopt (argc, argv, opt_string)) != -1) {
		
		if (c == 'l') {
			
			if (logFilePref) {
				cout << "Usage: "
					 << argv [0]
					 << " -l [logFilePref] -c [configFile] -p [portNo]"
					 << endl;
				return -1;				
			}
			
			logFilePref = strdup (optarg);
		}
		
		else if (c == 'c') {
			
			if (configFile) {
				cout << "Usage: "
					 << argv [0]
					 << " -l [logFilePref] -c [configFile] -p [portNo]"
					 << endl;
				return -1;
			}
			
			configFile = strdup (optarg);
		}
		
		else if (c == 'p') {
			
			if (portNo != 0) {
				cout << "Usage: "
					 << argv [0]
					 << " -l [logFilePref] -c [configFile] -p [portNo]"
					 << endl;
				return -1;
			}
			
			portNo = atoi (optarg);

			if (portNo <= 0) {
				cout << "Invalid port number" << endl;
				return -1;
			}			
		}

		//xing: DIS add level
		else if (c == 'x') {
			if (serverLevel != 0) {
				cout << "Usage: "
					 << argv [0]
					 << " -l [logFilePref] -c [configFile] -p [portNo]"
					 << endl;
				return -1;
			}

			serverLevel = atoi (optarg);
		}
	}
	
	if (!logFilePref || !configFile || (portNo == 0)) {
		cout << "Usage: "
			 << argv [0]
			 << " -l [logFilePref] -c [configFile] -p [portNo]"
			 << endl;
		return -1;
	}
	
	return 0;
}


int main(int argc, char *argv[])
{
	int rc;
	int       portNo;
	char     *logFilePref;
	char     *configFile;
	
	//xingxing DIS
	int 	 serverLevel;

	// Options
	if ((rc = getOpts (argc, argv,
					   logFilePref, configFile,
					   portNo, serverLevel)) != 0)
		return 1;	
	// XING DIS
	NetworkManager *netMgr = new NetworkManager (portNo, logFilePref,
												 configFile, serverLevel);
	
	//cout << "serverLevel: " << serverLevel << endl;
	//DIS
	char newPort[20];
		sprintf(newPort, "%d",portNo);
		//xing: level

		char databuf_new[100];
	    bzero(&databuf_new, sizeof(databuf_new));
		sprintf(databuf_new, "%s,%s,%s,%s",netMgr->getInterfaceAddress(), netMgr->getModel(1), netMgr->getModel(2), netMgr->getLoad());
	//	cout << "databuf_new: " << databuf_new << endl;
		int num = netMgr->calculate(databuf_new);
	//	cout << "value after calculation: " << num << std::endl;
		int port = portNo;
	//	cout << "Port after " << port << std::endl;
	//	int global_port = portNo;
		global_port = portNo;
		//xing level
		global_serverLevel = serverLevel;
		netMgr->serverLevel = global_serverLevel;
		//
		std::vector<payload>* ip_table = NetworkManager::getIpTable();
		payload pl;
		pl.ip = netMgr->getInterfaceAddress();
		pl.port = newPort;
		pl.value = num;
		// XING LEVEL
		pl.serverLevel = serverLevel;
		ip_table->push_back(pl);
	//	cout << "pl value: " << pl.value << endl;
	//	cout << "iptable size: " << ip_table->size() << endl;



		pthread_t maintainList;
		pthread_attr_t attr;
	   	//int rc;
	  	long t;
	   	void *status;

	   	// Initialize and set thread detached attribute
	   	pthread_attr_init(&attr);
	   	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

	   	//printf("Main: creating thread %ld\n", t);
	      	rc = pthread_create(&maintainList, &attr, &NetworkManager::basicgetscore_starter, (void *)&netMgr);
	      	if (rc) {
	         	printf("ERROR; return code from pthread_create() is %d\n", rc);
	         	exit(-1);
	        }

	//----

		netMgr -> run();

	//	DIS:
		rc = pthread_join(maintainList, &status);
	//-----
	
	return 0;
}
