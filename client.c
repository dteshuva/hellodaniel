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
sem_t *mutex ;
char *port;
char *host;
char *path1;
char *path2;
int turn;
char *ar;
int num;
int areTwo=0;
pthread_barrier_t barrier;
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
    int thread_num = *(int*)arg;
    int clientfd;
    char* path=turn==0?path1 : path2;

    //wait
    while(1){
        //printf("thread number %d\n",thread_num);
        sem_wait(&mutex[thread_num]);
      //  printf("\nEntered..\n");
        //critical section
        //sleep(4);
        // Send a GET request to the server
        // Establish a connection with the specified host and port
        clientfd = establishConnection(getHostInfo(host, port));

        if (clientfd == -1) {
            fprintf(stderr,
                    "[main:73] Failed to connect to: %s:%s%s \n",
                    host, port, ar );
            return NULL;
        }
        GET(clientfd, host, port, path);
        sem_post(&mutex[(thread_num+1) % num]);
        char buf[BUF_SIZE];
        // Receive the response from the server and print it to the standard output
        while (recv(clientfd, &buf, BUF_SIZE, 0) > 0) {
          // fputs(buf, stdout);  // Output the data to the terminal
            memset(buf, 0, BUF_SIZE);  // Clear the buffer for the next iteration
        }

        //signal
        //printf("\nJust Exiting...\n");
        sem_post(&mutex[thread_num]);
        close(clientfd);
        //  puts(path);
    }
    return NULL;
}
void* task(void* arg) {
    int x=0; //switch
    while (1) {
        // Send request for file
        printf("Thread %ld sending request\n", (long)arg);

        // Wait for barrier to synchronize with other threads
        pthread_barrier_wait(&barrier);

        // Wait for response
        printf("Thread %ld waiting for response\n", (long)arg);

        // Wait for barrier to synchronize with other threads
        pthread_barrier_wait(&barrier);
    }

    return NULL;
}
int main(int argc, char **argv) {
    int version;
    // Check if the number of command-line arguments is correct
    if (argc != 6&& argc!=7) {
        fprintf(stderr, "USAGE: ./httpclient <hostname> <port> <# of threads> <request path>\n");
        return 1;
    }
    num=atoi(argv[3]);
    host=argv[1];
    port=argv[2];
    version= strcmp(argv[4],"FIFO")==0 ? 0 : 1;
    path1=argv[5];
    ar=argv[3];
    if(argc==7){
        areTwo=1;
        path2=argv[6];
    }
    pthread_t threads[num];
    int thread_args[num];
    // FIFO version- initializing mutex
    if(version==0) {
        mutex = (sem_t *) malloc(num * sizeof(sem_t));
        for (int i = 0; i < num; i++) {
            if (sem_init(&mutex[i], 0, 1) != 0) {
                perror("sem_init");
                exit(EXIT_FAILURE);
            }
        }
    }
    // CONCUR- initialize a barrier
    else{
        if (pthread_barrier_init(&barrier, NULL, num) != 0){
            perror("pthred_barrier_init");
            exit(EXIT_FAILURE);
        }
    }
        for (int i = 0; i < num; i++) {
            thread_args[i]=i;
            if(pthread_create(&threads[i], NULL, version==0 ? thread : task, &thread_args[i]) != 0){
                perror("pthread_create");
                exit(EXIT_FAILURE);
            }
          //  sleep(2); //optional
            if (argc == 6 && version == 0) {
                turn = turn == 0 ? 1 : 0;
            }
        }
    if(version == 0){
        sem_post(&mutex[0]);
    }
        for (int i = 0; i < num; i++) {
            if(pthread_join(threads[i], NULL)!=0){
                perror("pthread_join");
                exit(EXIT_FAILURE);
            }
        }
    if(version == 1){
        if(pthread_attr_destroy(&barrier) != 0){
            perror("pthread_barrier_destroy");
            exit(EXIT_FAILURE);
        }
    }
    else {
        // Close the socket when it's no longer needed to release the resources associated with it and prevent leaks.
        for (int i = 0; i < num; i++) {
            if (sem_destroy(&mutex[i]) != 0) {
                perror("sem_detsroy");
                exit(EXIT_FAILURE);
            }
        }
        free(mutex);
    }
    return 0;
}
