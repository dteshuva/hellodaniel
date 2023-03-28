#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>


// readbyte 0xFF00AA00BB00CC00
// readbyte 0x0000000000000020
//readbyte 0x0000000000000021
// writebyte 0x0000000000000021 0xAC
// readbyte 0x0000000000000021
// writebyte 0x0000000000000021 0xAC
// writebyte 0x000000000000080A 0xFF
int page_size;
int vmpc;  // number of virtual pages of memory
int pmpc;  // number of physical pages of memory
int * used;
unsigned long long * addresses;
uint32_t virtual_addr = 0x00; // virtual address to be translated
uint32_t page_num = 0; // page number extracted from virtual address
// Function to get the physical address from the virtual address
void* get_physical_address(void** page_table, uint32_t virtual_addr,char* location) {
    uint32_t page_num = virtual_addr / page_size; // Extract page number from virtual address
    uint32_t pageAdd = page_num*page_size;
    if(page_num >= pmpc){
        return NULL;
    }
    void* physical_addr = page_table[page_num]; // Get corresponding physical page
    if (physical_addr == NULL) {
        return NULL; // Page not allocated, return NULL
    }
    physical_addr =(uint32_t*)physical_addr + (virtual_addr % page_size); // Add offset to physical address
    if(used[page_num] != 1){
        printf("created physical page at 0x%llX, mapped to virtual page at 0x%016llX\n", (unsigned long long)physical_addr, (unsigned long long)pageAdd);
        addresses[page_num]=(unsigned long long)physical_addr;
    }
    used[page_num]=1;
    return physical_addr;
}
void readAdd(void **page_table,uint32_t virAdd,char* location){
    void* physical_addr = get_physical_address(page_table, virAdd,location);
    if (physical_addr == NULL) {
        printf("readbyte: segmentation fault\n");
        return;
    }
    uint32_t value = *(uint32_t*)physical_addr; // Read byte from physical address
    printf("readbyte: VM location %s, which is PM location 0x%llX, contains value 0x%02X\n",
           location, (unsigned long long)physical_addr, value);

}
void writebyte(void** page_table, char* location, char* value) {
    uint32_t virtual_addr = (uint32_t)strtoul(location, NULL, 16); // Convert location to integer
    uint32_t page_num = virtual_addr / page_size; // Extract page number from virtual address
    uint32_t pageAdd= page_num*page_size;
    if(page_num >= pmpc){
        printf("writebyte: segmentation fault\n");
        return;
    }

    uint32_t byte_value = (uint32_t)strtoul(value, NULL, 16); // Convert value to integer byte
    uint32_t* physical_addr = (uint32_t*)page_table[page_num]; // Get corresponding physical page
    physical_addr += virtual_addr % page_size; // Add offset to physical address
    *physical_addr = byte_value; // Write byte to physical address
    if(used[page_num] != 1){
        printf("created physical page at 0x%llX, mapped to virtual page at 0x%016llX\n", (unsigned long long)physical_addr, (unsigned long long)pageAdd);
        addresses[page_num]=(unsigned long long)physical_addr;
    }
    used[page_num]=1;
    printf("writebyte: VM location %s, which is PM location 0x%llX, now contains value %s\n", location, (unsigned long long)physical_addr, value);
}
int main( int c,char **argv) {


    page_size = atoi(argv[1]);
    if ((page_size <= 0) || ((page_size & (page_size - 1)) != 0)) {
        printf("error: pagesize must be power of 2\n");
        exit(EXIT_FAILURE);
    }

    vmpc = atoi(argv[2]);
    pmpc = atoi(argv[3]);
    void *page_table[pmpc]; // array to store physical pages
    used= malloc(pmpc);
    addresses= malloc(pmpc);
    if (pmpc < vmpc) {
        printf("error: pmpc must be larger than or equal to vmpc\n");
        exit(EXIT_FAILURE);
    }


    // create page table with alignment
    for (int i = 0; i < pmpc; i++) {
        int ret = posix_memalign(&page_table[i], page_size, page_size * pmpc);
        if (ret != 0) {
            printf("Error: Failed to allocate memory for physical page %d\n", i);
            exit(EXIT_FAILURE);
        }
    }

    char input[10000];
    char *token;
    while (1) { // Continuously read input
        if (fgets(input, sizeof(input), stdin) == NULL) {
            printf("Error: failed to read input\n");
            exit(EXIT_FAILURE);
        }


        // Remove newline character from input string-not sure if necessary
        if (input[strlen(input) - 1] == '\n') {
            input[strlen(input) - 1] = '\0';
        }

        // Check if input is "exit", and break out of loop if it is
        if (strcmp(input, "exit") == 0) {
            break;
        }
        token = strtok(input, " ");

        if (strcmp(token, "readbyte") == 0) {
            token = strtok(NULL, " ");
            // translate virtual address to physical address
            uint32_t virtual_addr = strtoul(token, NULL, 16);
            readAdd(page_table, virtual_addr,token);

        } else if (strcmp(token, "writebyte") == 0) {
            token = strtok(NULL, " ");
            char *val = strtok(NULL, " ");
            writebyte(page_table, token, val);
        }
    }


    // free physical pages in order they were allocated
    for (int i = 0; i < pmpc; i++) {
        if(used[i]==1) {
            printf("physical page at 0x%llX destroyed\n", addresses[i]);
        }
        free(page_table[i]);
    }
    free(used);
    free(addresses);
    return 0;
}
