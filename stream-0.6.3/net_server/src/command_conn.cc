/**
 *
 * Xing 2010.10.15: User and Pwd are added.
 * 2010.10.20: MLS control are added.
 */
#include <stdlib.h>
#include <stdio.h>
//add by xing
#include <cstring>

//DIS
#include<sys/unistd.h>
#include<errno.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<netdb.h>
#include<vector>
#include "net_mgr.h"
//

#ifndef _NET_MGR_
#include "net_mgr.h"
#endif

#ifndef _COMMAND_CONN_
#include "command_conn.h"
#endif

//added by Xing
 int CURRENTSERVERID = -1;
#ifndef _NET_UTIL_
#include "net_util.h"
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

#define LOG (log_with_id ())

#include <iostream>
#include <fstream>
#include <cstring>
using namespace Network;
using namespace std;

//int CURRENTSERVERID;
CommandConnection::CommandConnection (int             id,
									  int             sockfd,
									  NetworkManager *net_mgr,
									  ostream        &_LOG)
	: logStream (_LOG)
{
	this -> id     = id;
	this -> sockfd = sockfd;
	this -> net_mgr = net_mgr;
	this -> server = 0;
	this -> b_server_started  = false;
	this -> num_out_conns = 0;
	this -> num_in_conns = 0;
	this -> bEnd = false;
	this ->trustedRR = net_mgr->trustedRR;
	// DIS
	this ->master = true;
	//-------

}

void CommandConnection::run()
{
	int rc;
	Command command;
	
	bool slaveflag = false;

	LOG << "Starting Up" << endl;
	
	// We need to register the command connection with the network manager
	if ((rc = net_mgr -> registerCommandConn (id)) != 0) {
		
		LOG << "Error registering with the netmgr"
			<< endl;
		
		return;
	}
	
	// Do authentication before creating the server instance.
	// First message should be the user name
	if ((rc = get_command(command)) != 0 ) {
				LOG << "Error getting command" << endl;
				return;
			}
	//Xing: if it is a slave, the first command is BEGIN_APP_S
	// Issue: What if this server serves as master as well as slave?
	if(command == 2){
		cout << "the first command is BEGIN_APP_S, it is a slave" << endl;
		slaveflag = true;
	}
	// Xing: for the master
	if(!slaveflag){

	if ((rc = handle_command(command)) != 0) {
				LOG << "Error handling command" << endl;
				return;
			}

	// Second message should be the password
	if ((rc = get_command(command)) != 0) {
				LOG << "Error getting command" << endl;
				return;
			}

	if ((rc = handle_command(command)) != 0) {
				LOG << "Error handling command: Authentication fails" << endl;
				return;
			}

	LOG << "Authentication ends. Now we can create server instance" << endl;
	}
	// Create an instance of the DSMS server
	if ((rc = initServer ()) != 0) {
		return;
	}
	
	// For the slave
	if(slaveflag){
		if ((rc = handle_command(command)) != 0) {
					LOG << "Error handling command: Authentication fails" << endl;
					return;
				}
	}

	while(!bEnd) {
		
		if ((rc = get_command(command)) != 0) {			
			LOG << "Error getting command" << endl;			
			return;
		}
		
		if ((rc = handle_command(command)) != 0) {
			LOG << "Error handling command" << endl;
			return;
		}
	}
	
	LOG << "Terminating" << endl;
	
	return;
}
// Xing; Change from 11 to 13
//Xing: DIS, change from 13 to 14 since BEGIN APP S
static const int num_commands = 14;

// Saved password and level
string savedPwd;
string savedLv;

static const char *comm_strings [] = {
	"AUTH USER",
	"AUTH PWD",
	// DIS
	"BEGIN APP S",
	"BEGIN APP",
	"REGISTER INPUT",
	"REGISTER QUERY",
	"REGISTER OUTPUT QUERY",
	"REGISTER NAME",
	"END APP",
	"GENERATE PLAN",
	"EXECUTE",
	"REGISTER MONITOR",
	"RESET",
	"TERMINATE"
};

static const Command comms [] = {
	AUTH_USER,
	AUTH_PWD,
	//dis
	BEGIN_APP_S,
	BEGIN_APP,
	REGISTER_INPUT,
	REGISTER_QUERY,
	REGISTER_QUERY_OUT,
	REGISTER_VIEW,	
	END_APP,
	GEN_PLAN,
	EXECUTE,
	REGISTER_MONITOR,
	RESET,
	TERMINATE
};

static const int COMM_BUF_LEN = 1024;

int CommandConnection::get_command(Command &command)
{
	int  rc;
	int  comm_len;
	char comm_buf [COMM_BUF_LEN];	
	
	if ((rc = NetUtil::getMessage(sockfd, comm_buf,
								  COMM_BUF_LEN, comm_len)) != 0) {
		LOG << "Error getting a message"
			<< endl;
		
		return -1;
	}

	for (int c = 0 ; c < num_commands ; c++) {
		
		if (strcmp (comm_strings [c], comm_buf) == 0) {
			command = comms [c];
			
			LOG << "Received command: "
				<< comm_strings [c]
				<< endl;
			
			return 0;
		}
		
	}	
	
	// Unknown command
	LOG << "Unknown command: "
		<< comm_buf
		<< endl;
	
	return -1;
}

int CommandConnection::handle_command(Command command)
{
	switch(command) {
		// Authentication
	case AUTH_USER:
		return handle_auth_user();

	case AUTH_PWD:
		return handle_auth_pwd();

		// DIS
	case BEGIN_APP_S:
			cout << "CommandConnection::handle_command: " << "BEGIN_APP_S" << endl;
			master = false;
			return handle_begin_app();

	case BEGIN_APP:
		cout << "CommandConnection:::handle_command: " << "BEGIN_APP" << endl;
		return handle_begin_app();

	case REGISTER_INPUT:
		cout << "CommandConnection::" << "REGISTER_INPUT" << endl;
		return handle_reg_input();

	case REGISTER_QUERY:
		cout << "CommandConnection::" << "REGISTER_QUERY" << endl;
		return handle_reg_query();

	case REGISTER_QUERY_OUT:
		return handle_reg_out_query();

	case REGISTER_VIEW:
		return handle_reg_view();
		
	case END_APP:
		return handle_end_app();
		
	case GEN_PLAN:
		return handle_gen_plan();
		
	case EXECUTE:
		return handle_execute();
		
	case REGISTER_MONITOR:
		return handle_reg_mon();

	case RESET:
		return handle_reset ();

	case TERMINATE:
		return handle_terminate ();
		
	default:
		// should never come
		ASSERT (0);
		break;
	}
	
	return 0;
}

int CommandConnection::handle_begin_app()
{
	int rc;
	//DIS
	int retVal;
	//----------
	
	if ((rc = server -> beginAppSpecification ()) != 0) {
		LOG << "Server::beginAppSpecification() error" << endl;			
		send_error_code (rc);
		
		return 0;
	}
	// DIS
	if (master) {
		for (int i = 1; i < CommandConnection::getNoOfIps(); i++) {
			payload *p = CommandConnection::getPayload(i);
			cout << p->ip.c_str() << "::" << p->port.c_str() << " :: Reading Socket" << endl;
			CommandConnection::setSocketId(i, CommandConnection::getSockDescriptor((char *)(p->ip.c_str()),(char *)(p->port.c_str())));
			cout << p->ip.c_str() << "::" << p->port.c_str() << " :: Socket: " << p->socketId << endl;
			retVal=CommandConnection::sendCommands(p->socketId,"COMMAND_CONN");

			retVal=CommandConnection::sendCommands(p->socketId,"BEGIN APP S");
		}
	}

	// ----------
	
	send_error_code (0);
	return 0;
}

void CommandConnection::savePassword(string c){
	pwd = c;
	// LOG << "The password in savePwd: " << savedPwd << endl;
}

string CommandConnection::getSavedPwd(){
	return pwd;
}

void CommandConnection::saveLevel(string c){
	userLevel = c;
}

string CommandConnection::getSavedLv(){
	return userLevel;
}



/// debug
#include <fstream>
using namespace std;

int CommandConnection::handle_auth_user()
{
	int              rc;

	// Get the register input string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;

	// Register input string:
//	LOG << "TEST The USER wants to connected: " << msg_buf << endl;
	// xing Debug: Find the current execution path NOT WORKING Path!
/*
	char buf[256];
	readlink("/proc/self/exe",buf,sizeof(buf));
	LOG << "Current Execution path: " << buf << endl;
*/



	// user available check
      char a[100];
      const char *b;
      // For specific user, i for username, j for password, k for level
      int i,j,k;
      int errorCode = 1;
	  string line, subsPwd, subsLv;
	//  string filename = "authentication.txt";
	//  ifstream myfile;
	//  myfile.open(filename, ifstream::in);
	  ifstream myfile("stream-0.6.3/authentication.txt");

	  if (myfile.is_open())
	  {
	    while (!myfile.eof())
	    {
	      getline(myfile, line);
	      b = line.c_str();
	   //   LOG << "TEST line record: " << line << endl;
	      i = 0;
	      while(b[i]!= ',') {
	   // 	  LOG << "b character record: " << b[i] << endl;
	    	  i++;
	      }
	  //    LOG << "b character is finished: "  << endl;
	      strncpy(a, b, i);
	 //     LOG << "strcpy is done "  << endl;
	    if(strncmp(a, msg_buf, i) == 0) {
	    	user = msg_buf;
	    //	LOG << "TEST The user: " << user << " is found" << endl;
	    	 errorCode = 0;
	    	 // Save the password for this user for further checking
	    	 i=i+1;
	         for(;b[i]!=',';i++){
	        	subsPwd += b[i];
	    	 }

	         //LOG << "The password in b: " << b << endl;
	        // LOG << "The password in subsPwd: " << subs << endl;
	         // call and save password
	         savePassword(subsPwd);

	         i=i+1;
	        for(;b[i]!='\0';i++){
	        	 subsLv += b[i];
	       	 }
	  //      LOG << "TEST The level in subsLv: " << subsLv << endl;

	        //save user level
	        //Xing: DIS level matching test
	        if(this->net_mgr->serverLevel != atoi(subsLv.c_str())){
	        	cout << "user level != server level, connection denied." << endl;
	        	errorCode = 2;
	        	send_error_code(errorCode);
	        }
	        saveLevel(subsLv);

	    	 break;
	      }

	    }
	    myfile.clear();
	    myfile.close();
	  }

	  else {
		  LOG << "Unable to open file" << endl;

	  }

	//------------------------------------

	send_error_code (errorCode);
	if(errorCode == 0)
		return 0;
	else
		return 1;
}


// Authentication of password
int CommandConnection::handle_auth_pwd()
{
	int              rc;

	// Get the register input string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;

	// Register input string:
//	LOG << "TEST The PASSWORD entered by client: " << msg_buf << endl;

	// password match check
	int errorCode =1;
//	LOG << "TEST the saved password:" << getSavedPwd() << endl;
	if(strcmp(getSavedPwd().c_str(), msg_buf) == 0) {
		   // 	LOG << "TEST The password is matching" << endl;
		    	 errorCode = 0;
		      }

	// Might add password checking here....
	send_error_code (errorCode);
	if(errorCode == 0)
			return 0;
		else
			return 1;
}

// queries should return with security level for MLS management. The security info is return
// create input connection from server.
int CommandConnection::handle_reg_input()
{
	int              rc;
	int              input_id;
	InputConnection *input_conn;
	//DIS
	int retVal;
	//.....

	// Get the register input string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;

	// Register input string:
	LOG << "Register Input String: " << msg_buf << endl;

	if (num_in_conns >= MAX_INPUT_CONN) {
 		LOG << "Too many input connections" << endl;
		send_error_code (-1);
		return -1;
	}

	// Xing: Create an input connection to handle the input, and the userLevel is passed!
	if ((rc = net_mgr -> createInputConn (input_id, input_conn,userLevel)) != 0) {
		send_error_code (rc);
		return rc;
	}

	input_conn->setMaster(master);

	in_conns [num_in_conns++] = input_conn;

	// Register the input with the server
	if ((rc = server -> registerBaseTable (msg_buf, (unsigned int)msg_len,
										   input_conn)) != 0) {
		send_error_code (rc);
		// Server's error - not our business?
		return 0;
	}
	//DIS
	if (master) {
		for (int i = 1; i < CommandConnection::getNoOfIps(); i++) {
			cout << "CommandConnection::handle_reg_input():CommandConnection::getNoOfIps(): "<<
					CommandConnection::getNoOfIps() << endl;
			payload *p = CommandConnection::getPayload(i);
			LOG << "SOCKET: " << p->socketId << endl;
			retVal=CommandConnection::sendCommands(p->socketId,"REGISTER INPUT");
			CommandConnection::setInputId(i, CommandConnection::sendCommands(p->socketId,msg_buf));

			LOG << "inputID: " << p->inputId << endl;
		}
	}
	//------
	send_error_code (0);
	send_id (input_id);

	return 0;
}

/* original stream-0.6.0 version
int CommandConnection::handle_reg_input()
{
	int              rc;
	int              input_id;
	InputConnection *input_conn;
	
	// Get the register input string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;
	
	// Register input string:
	LOG << "Register Input String: " << msg_buf << endl;

	if (num_in_conns >= MAX_INPUT_CONN) {
 		LOG << "Too many input connections" << endl;
		send_error_code (-1);
		return -1;
	}
	
	// Create an input connection to handle the input
	if ((rc = net_mgr -> createInputConn (input_id, input_conn)) != 0) {
		send_error_code (rc);				
		return rc;
	}

	in_conns [num_in_conns++] = input_conn;
	
	// Register the input with the server
	if ((rc = server -> registerBaseTable (msg_buf, (unsigned int)msg_len,
										   input_conn)) != 0) {
		send_error_code (rc);
		// Server's error - not our business?
		return 0;
	}
	
	send_error_code (0);
	send_id (input_id);
	
	return 0;
}
*/

int CommandConnection::handle_reg_query ()
{
	int rc;
	unsigned int query_id;
	
	// Get the register input string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;
	
	LOG << "(Internal) Query String: " <<  msg_buf << endl;
	
	if ((rc = server -> registerQuery (msg_buf, (unsigned int)msg_len, 0,
									   query_id)) != 0) {		
		LOG << "Error registering query: " <<  msg_buf << endl;
		send_error_code (rc);
		return 0;
	}
	
	if ((rc = server -> getQuerySchema (query_id,
										planBuf,
										PLAN_BUF_LEN)) != 0) {
		LOG << "Error getting schema from the server" << endl;
		send_error_code (rc);
		return 0;
	}
	
	if ((rc = send_error_code (0)) != 0)
		return rc;
	
	if ((rc = send_id (query_id)) != 0)
		return rc;
	
	if ((rc = send_mesg (planBuf, strlen(planBuf))) != 0)
		return rc;
	
	return 0;
}

int CommandConnection::handle_reg_out_query()
{	
	int rc;
	unsigned int query_id;
	int out_id;
	OutputConnection *out_conn;
	//DIS
	int retVal;
	//---
	
	// Get the query string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;
	
	LOG << "(Output) Query String: " << msg_buf << endl;

	if (num_out_conns >= MAX_OUTPUT_CONN) {
 		LOG << "Too many output connections" << endl;
		send_error_code (-1);
		return -1;
	}
	
	// Get an output connection object to relay query output to client
	if ((rc = net_mgr -> createOutputConn (out_id, out_conn)) != 0) {
		LOG << "Error creating an output connection" << endl;
		send_error_code (rc);
		return rc;
	}	

	out_conn->setMaster(master);
	out_conns [num_out_conns++] = out_conn;
	
	// Register the query with the server
	if ((rc = server -> registerQuery (msg_buf, (unsigned int)msg_len,
									   out_conn,
									   query_id)) != 0) {
		// Xing: LOG Test
		LOG << "Error rc: " <<  rc << endl;
		LOG << "Error registering query: " <<  msg_buf << endl;
		send_error_code (rc);
		return 0;
	}
	
	// Get the schema of the output
	if ((rc = server -> getQuerySchema (query_id,
										planBuf,
										PLAN_BUF_LEN)) != 0) {
		
		LOG << "Error getting schema from the server" << endl;
		send_error_code (rc);
		return 0;
	}	
	//DIS
	if (master) {
		for (int i = 1; i < CommandConnection::getNoOfIps(); i++) {
			payload *p = CommandConnection::getPayload(i);
			LOG << "Socket: " << p->socketId << endl;
	//		int sock_tcp = p->socketId;
			retVal=CommandConnection::sendCommands(p->socketId,"REGISTER OUTPUT QUERY");
			CommandConnection::setOutputId(i, CommandConnection::sendCommands(p->socketId,msg_buf));
			LOG << "SoutputId: " << p->outputId << endl;
		}
	}
	//-----
	if ((rc = send_error_code (0)) != 0)
		return rc;
	
	if ((rc = send_id ((int)query_id)) != 0)
		return rc;
	
	// Xing: Test
	//cout << "command_conn.cc: out_id: " << out_id << endl;
	if ((rc = send_id (out_id)) != 0)
		return rc;
	
	if ((rc = send_mesg (planBuf, strlen(planBuf))) != 0)
		return rc;
	
	return 0;
}

int CommandConnection::handle_reg_view()
{	
	int               rc;
	int               query_id;

	// Get the query id
	if ((rc = get_id (query_id)) != 0)
		return rc;
	
	// Get the view schema
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;
	
	LOG << "Query[" << query_id << "] = " << msg_buf << endl;
	
	// Register the view with the server
	if ((rc = server -> registerView ((unsigned int)query_id,
									  msg_buf,
									  (unsigned int)msg_len)) != 0) {
		LOG << "Error registering view with server" << endl;
		send_error_code (rc);
		return 0;
	}
	
	send_error_code (0);
	
	// Send metaplan
	// [Todo]
	
	return 0;
}

int CommandConnection::handle_end_app()
{
	int rc;
	//DIS
	int retVal;
	//---
	//Added by Xing:
	LOG << "current server ID: " << CURRENTSERVERID << endl;
	if ((rc = server -> endAppSpecification (CURRENTSERVERID, trustedRR, atoi(userLevel.c_str()))) != 0) {

		LOG << "server::endAppSpec() error" << endl;
		send_error_code (rc);
		return 0;
	}
	//DIS
	if (master) {
//		payload *p = &(iptable[0]);
		for (int i = 1; i < CommandConnection::getNoOfIps(); i++) {
			payload *p = CommandConnection::getPayload(i);
			LOG << "Socket: " << p->socketId << endl;
			retVal=CommandConnection::sendCommands(p->socketId,"END APP");
		}
	}
	//---
	send_error_code (0);
	
	return 0;
}

int CommandConnection::handle_gen_plan ()
{
	int rc;
	//DIS
	int retVal;
	//---
	
	if ((rc = server -> getXMLPlan (planBuf, PLAN_BUF_LEN)) != 0) {
		LOG << "Server: getXMLPlan error" << endl;
		send_error_code (rc);
		return 0;
	}
	//DIS
	if (master) {
//		payload *p = &(iptable[0]);
		for (int i = 1; i < CommandConnection::getNoOfIps(); i++) {
			payload *p = CommandConnection::getPayload(i);
			LOG << "Socket: " << p->socketId << endl;
			retVal=CommandConnection::sendCommands(p->socketId,"GENERATE PLAN");
		}
	}
	//----

	if ((rc = send_error_code (0)) != 0)
		return rc;

	if ((rc = send_mesg (planBuf, strlen(planBuf))) != 0)
		return rc;
	
	return 0;
}

static void *start_server (void *s) {
	Server *server = (Server *)s;
	server -> beginExecution ();
	return 0;
}

int CommandConnection::handle_execute ()
{
	int rc;
	//DIS
	int retVal;
	//-----
	
	// start off the server thread
	rc = pthread_create (&server_thread,
						 NULL,
						 &start_server,
						 (void*)server);
	
	if (rc != 0) {
		LOG << "Error creating the server thread" << endl;		
		send_error_code (-1);
		return -1;
	}
	//DIS
	if (master) {
		for (int i = 1; i < CommandConnection::getNoOfIps(); i++) {
			payload *p = CommandConnection::getPayload(i);
			LOG << "Socket: " << p->socketId << endl;
			retVal=CommandConnection::sendCommands(p->socketId,"EXECUTE");
		}
	}
	//----
	
	b_server_started = true;
	
	if ((rc = send_error_code (0)) != 0)
		return rc;
	
	// TODO
	return 0;
}

int CommandConnection::handle_reset ()
{
	int rc;

	LOG << "handle reset" << endl;
	
	// if we have started off the server thread, stop it
	if (b_server_started) {
		if ((rc = server -> stopExecution ()) != 0) {
			LOG << "Error stopping the server" << endl;
			send_error_code (-1);
			return -1;
		}
		
		pthread_join (server_thread, NULL);
		b_server_started = false;
	}
	
	delete server;

	LOG << "dump after delete server " << endl;
	
	// Stop all the output connections
	for (int o = 0 ; o < num_out_conns ; o++) {
		out_conns[o] -> end ();
		out_conns[o] = 0;
	}
	num_out_conns = 0;

	// Stop all input connections
	for (int i = 0 ; i < num_in_conns ; i++) {
		in_conns [i] -> end ();
		in_conns [i] = 0;
	}
	num_in_conns = 0;
	
	// Create a new server
	if ((rc = initServer ()) != 0) {
		LOG << "Error starting the server" << endl;
		send_error_code (-1);
		return -1;
	}
	
	send_error_code (0);
	return 0;
}

int CommandConnection::handle_reg_mon ()
{
	int rc;
	unsigned int mon_id;
	int out_id;
	OutputConnection *out_conn;
	
	// Get the query string
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;
	
	LOG << "Monitor Query String: " << msg_buf << endl;
	
	if (num_out_conns >= MAX_OUTPUT_CONN) {
		LOG << "Too many output connections" << endl;
		send_error_code (-1);
		return -1;
	}

	// Get an output connection object to relay monitor output to client
	if ((rc = net_mgr -> createOutputConn (out_id, out_conn)) != 0) {
		LOG << "Error creating an output connection" << endl;
		send_error_code (rc);
		return rc;
	}
	out_conns [num_out_conns++] = out_conn;

	// Register the monitor with the server
	if ((rc = server -> registerMonitor (msg_buf, (unsigned int)msg_len,
										 out_conn, mon_id)) != 0) {		
		LOG << "Error registering monitor: " <<  msg_buf << endl;
		send_error_code (rc);
		return 0;
	}

	// Get the schema of the output
	if ((rc = server -> getQuerySchema (mon_id, planBuf, 
										PLAN_BUF_LEN)) != 0) {		
		LOG << "Error getting schema from the server" << endl;
		send_error_code (rc);
		return 0;
	}

 	if ((rc = send_error_code (0)) != 0)
		return rc;

	if ((rc = send_id ((int)mon_id)) != 0)
		return rc;

	if ((rc = send_id (out_id)) != 0)
		return rc;


	if ((rc = send_mesg (planBuf, strlen(planBuf))) != 0)
		return rc;

	if ((rc = server -> getXMLPlan (planBuf, PLAN_BUF_LEN)) != 0) {
		LOG << "Server: getXMLPlan error" << endl;
		return rc;
	}
	
	if ((rc = send_mesg (planBuf, strlen(planBuf))) != 0)
		return rc;
	
	return 0;	
}

int CommandConnection::handle_terminate ()
{
	int rc;
	
	// if we have started off the server thread, stop it
	if (b_server_started) {
		if ((rc = server -> stopExecution ()) != 0) {
			send_error_code (-1);
			return -1;
		}
		
		pthread_join (server_thread, NULL);
		b_server_started = false;
	}
	
	delete server;
	
	// Stop all the output connections
	for (int o = 0 ; o < num_out_conns ; o++) {
		out_conns[o] -> end ();
		out_conns[o] = 0;
	}
	num_out_conns = 0;
	
	bEnd = true;
	
	send_error_code (0);
	
	return 0;
}

static const int MAX_FILE_NAME = 256;
int CommandConnection::initServer ()
{
	int         rc;
	char       *logFilePref;
	char        fileNameBuf [MAX_FILE_NAME];
	ofstream    *dsmsLogStr;
	const char *configFile;
	
	// We don't want all servers to write to the same log: The server will
	// write  to   some  file  of  the   form  <logFilePrefix><id>,  where
	// <logFilePrefix>  is a  common prefix  set at  the start  of network
	// manager, and <id> is the identifier of this thread.	
	logFilePref = net_mgr -> getLogFilePref ();
	int serverId = net_mgr -> getNewServerId ();
	sprintf (fileNameBuf, "%s%d", logFilePref, serverId);
	//added by Xing:
    CURRENTSERVERID = serverId;
	//currentServerID = serverId;
	LOG << "Creating Server [" << serverId << "]" << endl;
	
	dsmsLogStr = new ofstream ();
	dsmsLogStr -> open (fileNameBuf, ofstream::out);
	
	server = Server::newServer (*dsmsLogStr);

	configFile = net_mgr -> getConfigFile ();
	
	if ((rc = server -> setConfigFile (configFile)) != 0) {
		LOG << "Error setting server config file" << endl;
		return rc;
	}
	
	return 0;
}

ostream& CommandConnection::log_with_id ()
{
	logStream << "CommandConn ["
			  << id
			  << "]: ";
	
	return logStream;
}
		
int CommandConnection::get_id (int &id)
{
	int rc;
	int msg_len;
	
	if ((rc = get_mesg (msg_buf, MSG_BUF_LEN, msg_len)) != 0)
		return rc;

	id = atoi (msg_buf);
	return 0;
}

int CommandConnection::get_mesg (char *buf, int buf_len, int &msg_len)
{
	int rc;

	if ((rc = NetUtil::getMessage (sockfd, buf, buf_len, msg_len)) != 0) {

		LOG << "Error getting a message" << endl;
		return rc;
	}

	return 0;
}

int CommandConnection::send_error_code (int err_code)
{
	int rc;

	if ((rc = NetUtil::sendErrorCode (sockfd, err_code)) != 0) {

		LOG << "Error sending error code" << endl;
		return -1;
	}

	return 0;
}

int CommandConnection::send_id (int id)
{
	int rc;

	sprintf (msg_buf, "%d", id);
	
	if ((rc = NetUtil::sendMessage (sockfd, msg_buf,
									strlen(msg_buf))) != 0) {
		LOG << "Error sending message" << endl;
		return rc;
	}
	return 0;
}

int CommandConnection::send_mesg (char *msg, int msg_len)
{
	int rc;
	
	if ((rc = NetUtil::sendMessage (sockfd, msg, msg_len)) != 0) {		
		LOG << "Error sending a message" << endl;
		return rc;
	}
	
	return 0;
}
