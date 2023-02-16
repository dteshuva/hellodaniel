#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/socket.h>

#define BUF_SIZE 100
sem_t mutex;
char *port;
char *host;
char *path1;
char *path2;
int turn;
char *ar;
int num;
// Get host information (used to establish connection)
struct addrinfo *getHostInfo(char* host, char* port) {
    int r;
    struct addrinfo hints, *getaddrinfo_res;
    // Setup hints for getaddrinfo function
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;   // use IPV4
    hints.ai_socktype = SOCK_STREAM;  // use TCP socket
    // get address information based on the host name and port number
    if ((r = getaddrinfo(host, port, &hints, &getaddrinfo_res))) {
        // error handling if getaddrinfo fails
        fprintf(stderr, "[getHostInfo:21:getaddrinfo] %s\n", gai_strerror(r));
        return NULL;
    }
    return getaddrinfo_res;
}

// Establish connection with the host
int establishConnection(struct addrinfo *info) {
    if (info == NULL) return -1;

    int clientfd;
    // iterate through the linked list of addresses
    for (;info != NULL; info = info->ai_next) {
        // create a socket using the current address
        if ((clientfd = socket(info->ai_family,
                               info->ai_socktype,
                               info->ai_protocol)) < 0) {
            // handle error if socket creation fails
            perror("[establishConnection:35:socket]");
            continue;
        }
        // attempt to connect to the current address
        if (connect(clientfd, info->ai_addr, info->ai_addrlen) < 0) {
            // close the socket and continue with the next address if the connection fails
            close(clientfd);
            perror("[establishConnection:42:connect]");
            continue;
        }
        // free memory used by the linked list and return the client file descriptor if connection succeeds
        freeaddrinfo(info);
        return clientfd;
    }
    // free memory used by the linked list if the connection fails
    freeaddrinfo(info);
    return -1;
}

// Send GET request to the host
void GET(int clientfd, char *host, char *port, char *path) {
    char req[1000] = {0};
    // create a GET request message
    sprintf(req, "GET %s HTTP/1.1\r\nConnection: close\r\nHost: %s:%s\r\n\r\n", path, host, port);
    // send the GET request using the connected socket
    send(clientfd, req, strlen(req), 0);
}
void* thread(void* arg)
{

    int clientfd;
    //wait
    printf("cmon bro!!! \n");
    sem_wait(&mutex);
    printf("\nEntered..\n");
    //critical section
    printf("cmon bro!!! \n");
    sleep(4);
    // Send a GET request to the server
    printf("cmon bro!!! \n");
    sem_init(&mutex, 0, 1);
    // Establish a connection with the specified host and port
    clientfd = establishConnection(getHostInfo(host, port));
    printf("cmon bro!!! \n");
    if (clientfd == -1) {
        fprintf(stderr,
                "[main:73] Failed to connect to: %s:%s%s \n",
                host, port, ar );
        return NULL;
    }
    printf("cmon bro!!! \n");
    GET(clientfd, host, port, turn==0?path1 : path2);
    printf("cmon bro!!! \n");
    char buf[BUF_SIZE];
    // Receive the response from the server and print it to the standard output
    while (recv(clientfd, &buf, BUF_SIZE, 0) > 0) {
        printf("cmon bro!!! \n");
        fputs(buf, stdout);  // Output the data to the terminal
        memset(buf, 0, BUF_SIZE);  // Clear the buffer for the next iteration
    }

    //signal
    printf("\nJust Exiting...\n");
    sem_post(&mutex);
    close(clientfd);
    return NULL;
}

int main(int argc, char **argv) {

    // Check if the number of command-line arguments is correct
    if (argc != 5&& argc!=6) {
        fprintf(stderr, "USAGE: ./httpclient <hostname> <port> <# of threads> <request path>\n");
        return 1;
    }
    num=atoi(argv[3]);
    pthread_t threads[num];
    ar=argv[3];
    for(int i=0; i<num;i++){
        pthread_create(&threads[i],NULL, thread,&mutex); // may replace last arg with NULL
        sleep(2); //optional
        if(argc==6){
            turn= turn==0?1:0;
        }
    }
    for(int i=0; i<atoi(argv[3]); i++){
        pthread_join(threads[i],NULL);
    }
    // Close the socket when it's no longer needed to release the resources associated with it and prevent leaks.
    sem_destroy(&mutex);
    return 0;
}
