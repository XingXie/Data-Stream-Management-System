/**
 *
 * Xing 2010.10.15: User and Pwd are added.
 */
#ifndef _COMMAND_CONN_
#define _COMMAND_CONN_

#ifndef _THREAD_
#include "thread.h"
#endif

#ifndef _IN_CONN_
#include "in_conn.h"
#endif

#ifndef _OUT_CONN_
#include "out_conn.h"
#endif

#ifndef _SERVER_
#include "interface/server.h"
#endif

//DIS
#ifndef _NET_UTIL_
#include "net_util.h"
#endif
//----

#include "execution/scheduler/round_robin.h"
#include <pthread.h>

///DIS
#include<sys/unistd.h>
#include<errno.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<netdb.h>
#include<iostream>
#include <cstdio>
#include "net_mgr.h"

#include <cstring>
#include <stdio.h>
#include <stdlib.h>

#include <ostream>




using namespace std;

namespace Network {

	class NetworkManager;
	//added by Xing
	//class Execution::RoundRobinScheduler;
	/// List of commands
	enum Command {
		AUTH_USER,
		AUTH_PWD,
		// DIS
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
	
	class CommandConnection : public Thread {
	private:
		//added by Xing
		class Execution::RoundRobinScheduler *trustedRR;

		/// Unique for this connection
		int id;
		
		/// Event log stream
		ostream &logStream;
		
		/// Socket for the TCP connection
		int sockfd;

		/// has the command connection terminated?
		bool bEnd;
		
		/// Network manager who created us
		NetworkManager *net_mgr;
		
		/// DSMS server
		Server *server;

		/// Server thread (once it starts executing)
		pthread_t server_thread;
		
		/// Has the server started (serverThread created)?
		bool b_server_started;
		
		/// maximum nmber of output connections per comm conn
		static const int MAX_OUTPUT_CONN = 20;
		
		/// The active output connections of this command connection
		OutputConnection *out_conns [MAX_OUTPUT_CONN];
		
		/// The number of active output connections
		int num_out_conns;		
		
		/// Maximum no of input conns per comm conn
		static const int MAX_INPUT_CONN = 20;
		
		/// The active input connections of this command connection
		InputConnection *in_conns [MAX_INPUT_CONN];
		
		/// The number of active input conections
		int num_in_conns;
		
		/// Scratch space used for messages
		static const int MSG_BUF_LEN = 2048;
		
		//DIS
		static const int CMD_BUF_LEN = 1024;
		//--------

		char msg_buf [MSG_BUF_LEN];
		int msg_len;
		
		/// Space used for encoding plans and schema
		static const int PLAN_BUF_LEN = (1 << 20);
		
		/// 1 MB
		char planBuf [PLAN_BUF_LEN];
		
		/// For Authentication
		string user, pwd;
		string userLevel;
	   //string savedPwd;
		//----------DIS
		//Keep track of slave sockets
			int slave_sockets[MAX_OUTPUT_CONN];

			//Keep track of slave input connection ids
			static int slave_ic_ids[MAX_OUTPUT_CONN];

			//Keep track of slave output connection ids
			static int slave_oc_ids[MAX_OUTPUT_CONN];

			//Whether this connection refers to an original client
			bool master;
        //------------------------




	public:
		//added by xing:
		//		int currentServerID;
		CommandConnection (int id,
						   int sockfd,
						   NetworkManager *net_mgr,
						   std::ostream &LOG);
		int returnCurrentSeverID();
		
		virtual void run();
		
// DIS------------

		//index should be less than MAX_OUTPUT_CONN
		static int getInputId(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			return p->inputId;
		}

		static int getOutputId(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			return p->outputId;
		}

		static int getSocketId(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			return p->socketId;
		}

		static int getInSocketId(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			return p->insockId;
		}

		static int getOutSocketId(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			return p->outsockId;
		}

		static payload* getPayload(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			return p;
		}

		static void setSocketId(int index, int sid) {
			cout << "Setting SOCK ID: " << sid << " :: for INDEX: " << index << endl;
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			p->socketId = sid;
			cout << "Getting SOCK ID: " << p->socketId << " :: for INDEX: " << index << endl;
		}

		static void setInSocketId(int index, int sid) {
			cout << "Setting IN SOCK ID: " << sid << " :: for INDEX: " << index << endl;
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			p->insockId = sid;
			cout << "Getting IN SOCK ID: " << p->insockId << " :: for INDEX: " << index << endl;
		}

		static void setOutSocketId(int index, int sid) {
			cout << "Setting OUT SOCK ID: " << sid << " :: for INDEX: " << index << endl;
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			p->outsockId = sid;
			cout << "Getting OUT SOCK ID: " << p->outsockId << " :: for INDEX: " << index << endl;
		}

		static void setOutputId(int index, int oid) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			p->outputId = oid;
		}

		static void setInputId(int index, int iid) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			p->inputId = iid;
		}

		static void decrementTempValue(int index) {
			payload *p = &((*(NetworkManager::getIpTable()))[index]);
			p->tmpValue--;
		}

		static void resetTempValues() {
			int nIps = getNoOfIps();
			for (int x = 0; x < nIps; x++) {
				payload *p = &((*(NetworkManager::getIpTable()))[x]);
				p->tmpValue = static_cast<int>(p->value);
			}
		}

		static int getNoOfIps() {
			return (*(NetworkManager::getIpTable())).size();
		}

		static int getSockDescriptor(char *serverIpAddress, char *sPortNo) {
			int sock_tcp;
			/* Declaration Section - Socket Details */
			struct hostent *hostIp;
			struct sockaddr_in serv_addr; //Address of server
			/* Application call to create a new socket for tcp communication */
			if ((sock_tcp = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
				perror("Error in Socket call \n");
				exit(1); // To exit the program
			}
			/* Set server(remote) socket's information */
			bzero((char*) (&serv_addr), sizeof(serv_addr)); //clear the memory for server address
			/* Address Family to use */
			serv_addr.sin_family = AF_INET;
			/* Port Number to use */
			serv_addr.sin_port = htons(atoi(sPortNo));
			/* Set the IP address */
			serv_addr.sin_addr.s_addr = inet_addr(serverIpAddress);
			/* Set zero to the rest of the stuct variable */
			bzero(&(serv_addr.sin_zero), 8);
			/* Connecting to server */
			if (connect(sock_tcp, (struct sockaddr*) (&serv_addr),
					sizeof(serv_addr)) != 0) {
				perror("getSockDescriptor: Not able to establish a connection \n");
				exit(1); // To exit the program
			}

			return sock_tcp;

		}

		static int sendCommands(int sock_tcpWWW, char* Command) {
			int writeLength = 0;
			int readLen;
			char errorCode[CMD_BUF_LEN];
			char query_Id[CMD_BUF_LEN];
			char input_Id[CMD_BUF_LEN];
			char output_Id[CMD_BUF_LEN];
			char planBuf[MSG_BUF_LEN];
			int rc;
			int comm_len;
			char comm_buf[CMD_BUF_LEN];

			int commandLen = strlen(Command) + 1;
			uint32_t scommandLen = htonl(commandLen);
			uint32_t test;

			writeLength = write(sock_tcpWWW, &scommandLen, sizeof(int));
			writeLength = write(sock_tcpWWW, Command, commandLen);

			cout << "Command: " << Command << endl;

			if (strncmp(Command, "COMMAND_CONN", 12) == 0) {

				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "errorCode " << errorCode << "  comm_len " << comm_len
						<< endl;

			} else if (strncmp(Command, "BEGIN APP", 9) == 0) {
			//	cout << "in coommand_coon.h: BEGIN APP" << endl;
				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "errorCode " << errorCode << "  comm_len " << comm_len
						<< endl;
			} else if (strncmp(Command, "REGISTER INPUT", 14) == 0) {

			} else if (strncmp(Command, "Register Stream", 15) == 0) {
				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "errorCode " << errorCode << "  comm_len " << comm_len
						<< endl;

				if ((rc = NetUtil::getMessage(sock_tcpWWW, input_Id, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "input_Id " << input_Id << "  comm_len " << comm_len
						<< endl;

				return atoi(input_Id);
			} else if (strncmp(Command, "REGISTER OUTPUT", 15) == 0) {

			} else if (strncmp(Command, "Select", 6) == 0) {

				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "errorCode " << errorCode << "  comm_len " << comm_len
						<< endl;

				if ((rc = NetUtil::getMessage(sock_tcpWWW, query_Id, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "query_Id " << query_Id << "  comm_len " << comm_len
						<< endl;
				if ((rc = NetUtil::getMessage(sock_tcpWWW, output_Id, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "output_Id " << output_Id << "  comm_len " << comm_len
						<< endl;

				if ((rc = NetUtil::getMessage(sock_tcpWWW, planBuf, MSG_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "planBuf " << planBuf << "  comm_len " << comm_len << endl;

				return atoi(output_Id);
			} else if (strncmp(Command, "INPUT_CONN", 10) == 0) {
			//	cout << "CommandConnection_h::" << "INPUT_CONN" << endl;
				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "INPUT_CONN Error getting a message" << endl;

					return -1;
				}
		//		cout << "CommandConnection_h::" << "INPUT_CONN errorCode " << errorCode << "  comm_len "
		//				<< comm_len << endl;
			} else if (strncmp(Command, "END APP", 7) == 0) {
				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "errorCode " << errorCode << "  comm_len " << comm_len
						<< endl;
			}

			else if (strncmp(Command, "GENERATE PLAN", 13) == 0) {
				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "errorCode " << errorCode << "  comm_len " << comm_len
						<< endl;

				if ((rc = NetUtil::getMessage(sock_tcpWWW, planBuf, MSG_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "planBuf " << planBuf << "  comm_len " << comm_len << endl;

			} else if (strncmp(Command, "OUTPUT_CONN", 11) == 0) {

				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "OUTPUT_CONN Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "OUTPUT_CONN errorCode " << errorCode << "  comm_len "
						<< comm_len << endl;

			}

			else if (strncmp(Command, "EXECUTE", 7) == 0) {

				if ((rc = NetUtil::getMessage(sock_tcpWWW, errorCode, CMD_BUF_LEN,
						comm_len)) != 0) {
					cout << "CommandConnection_h::" << "EXECUTE Error getting a message" << endl;

					return -1;
				}
				cout << "CommandConnection_h::" << "EXECUTE errorCode " << errorCode << "  comm_len "
						<< comm_len << endl;
			}

			return 0;
		}


// --------------------



	private:
		int get_command (Command &command);
		int handle_command (Command command);
		
		// AUTH methods
		int handle_auth_user();
		int handle_auth_pwd();

		int handle_begin_app();
		int handle_reg_input();
		int handle_reg_query();
		int handle_reg_out_query();
		int handle_reg_view();
		int handle_end_app();
		int handle_gen_plan();
		int handle_execute();
		int handle_reset ();
		int handle_reg_mon();	
		int handle_terminate ();
		
		int initServer ();

		int get_id (int &);
		int get_mesg (char *, int, int&);
		int send_error_code (int);
		int send_id (int);
		int send_mesg (char *msg, int msg_len);
		
		void savePassword(string c);

		string getSavedPwd();

		void saveLevel(string c);

		string getSavedLv();

		ostream &log_with_id();

		static void returnServerID();
	};
}

#endif
