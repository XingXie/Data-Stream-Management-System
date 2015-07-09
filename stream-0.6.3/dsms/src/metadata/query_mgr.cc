
#ifndef _QUERY_MGR_
#include "metadata/query_mgr.h"
#endif

#ifndef _DEBUG_
#include "common/debug.h"
#endif
// add by xing
#include <cstring>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <cstring>
using namespace Metadata;
using namespace std;

QueryManager::QueryManager (ostream &_LOG)
	: LOG (_LOG)
{
	this -> numQueries = 0;
	this -> next = queryBuffer;



	for (unsigned int q = 0 ; q < MAX_QUERIES ; q++)
		queries [q] = 0;
}

QueryManager::~QueryManager() {}

int QueryManager::registerQuery (const char *qryStr,
								 unsigned int qryStrLen,
								 unsigned int &qryId, Semantic::Query semQuery)
{

	// Xing
	char *temp;

	// Sanity checks
	ASSERT (qryStr);
	ASSERT (qryStrLen > 0);
	if (strlen (qryStr) != qryStrLen) {
		LOG << "Query: " << qryStr << endl;
		LOG << "strlen(qryStr) = " << strlen(qryStr) << endl;
		LOG << "qryStrLen = " << qryStrLen << endl;
		ASSERT (0);
	}
	ASSERT (next - queryBuffer <= (int)QRY_BUF_LEN);
	
	// We don't have space?
	if (next - queryBuffer + qryStrLen >= QRY_BUF_LEN) {
		LOG << "QueryManager out of space" << endl;
		return -1;
	}
	
	// Exceeded query number limit
	if (numQueries >= MAX_QUERIES) {
		LOG << "QueryManager: maximum query limit exceeded" << endl;
		return -1;
	}
	
	// Xing: save semQuery
//	cout<< "query.cc: number of queries: " << numQueries+1 << endl;
	semQueryBuffer[numQueries] = semQuery;
//	cout<< "semQueryBuffer[0]: " << semQueryBuffer[0].gbyAttrs[0].attrId << endl;

//	cout<< "the semQueryBuffer[0]: " << semQueryBuffer[0].preds[0].right->u.sval << endl;

	//cout<< "after copy: the semQueryBuffer[0]: " << semQueryBuffer[numQueries].preds[0].right->u.sval << endl;
	// queries[queryId] retrieves the string for the query
	qryId = numQueries ++;
	queries [qryId] = next;
	
	strncpy (next, qryStr, qryStrLen + 1);
	next += (qryStrLen + 1);
	

	return 0;
}

