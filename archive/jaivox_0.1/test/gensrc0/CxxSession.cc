
#include "CxxSession.h"

#define BSIZE 4096
#define LSIZE 1024

int MaxWaitPeriods = 10;
int defaultWaitForMsg = 1;
int defaultWaitAfterSend = 1;

// make same as CxxResponder.cc
char terminateMessage [] = "JviaTerminate";
char requestUwho [] = "JviaWho";
char invalidMessage [] = "JviaInvalid";
char responseMessage [] = "JviaResponse";
char finishedMessage [] = "JviaFinished";
char requestFestival [] = "JviaFestival";

char who [] = "{action: JviaWho, from: PATsynthesizer, to: PATinterpreter, message: \"JviaWho\"}";

static int Count;

CxxSession::CxxSession (CxxServer* own, CxxSocket *link) {
    Owner = own;
    Link = link;

    Responder = new CxxResponder (this);
    Count++;
    sessionId = Count;
}

CxxSession::CxxSession () : CxxThread () {
    Responder = new CxxResponder (this);
    Count++;
    sessionId = Count;
}

int CxxSession::GetPort () {
    return ntohs (Link->addr.sin_port);
}

int CxxSession::Close () {
    Link -> closeSocket ();
    return 1;
}

void CxxSession::Initialize (CxxServer* own, CxxSocket *link) {
    char req[LSIZE];
    Owner = own;
    Link = link;
    if (Responder == NULL) Responder = new CxxResponder (this);
    gethostname (req, LSIZE);
    Host = req;
}


void CxxSession::RunAction () {
    int step = 0;
    int quit = 0;
	int initialized = 0;

    while (true) {
        sleep (defaultWaitForMsg);
        // the quit flag may have been raised somewhere
        if (quit == 1) break;
		if (!initialized) {
			Owner->execute ((char*)"send", (char*)"0", (char*)who);
			fprintf (stdout, "sent %s on session 0\n", who);
			initialized = 1;
			continue;
		}
        int charsRead = readFromSocket ();
        if (charsRead > 0) {
            fprintf (stdout, "CxxSession:%d Recived %s step %d\n", sessionId, buffer, step);
            CxxData* preply = Responder->Respond (buffer);
            if (preply == NULL) continue;
            char* replytext = preply -> toText ();
            fprintf (stdout, "CxxSession:%d Response %s at step %d\n", sessionId, replytext, step);
            char* action = preply->action;
            if (strcmp (action, terminateMessage) == 0) {
                quit = 1;
                continue;
            }
            else {
                strcpy (outbuffer, replytext);
            }
        }
        if (strlen (outbuffer) > 0) {
            int charsToSend = strlen (outbuffer);
            Link -> sendData (outbuffer, charsToSend);
        }
        sleep (defaultWaitAfterSend);
        step++;
    }
    // disconnect
    fprintf (stdout, "Trying to close down Link Id = %d\n", sessionId);
    Link -> closeSocket ();
    Stop ();
}

int CxxSession::readFromSocket () {
    // wait for a while
    // for (int i = 0; i < MaxWaitPeriods; i++)
    //    sleep (defaultWaitForMsg);

    int charsRead = Link -> getData (buffer, BSIZE);
    return charsRead;
}


