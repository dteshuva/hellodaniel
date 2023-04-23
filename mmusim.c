#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
/*
 * // readbyte 0xFF00AA00BB00CC00
// readbyte 0x0000000000000020
//readbyte 0x0000000000000021
// writebyte 0x0000000000000021 0xAC
// readbyte 0x0000000000000021
// writebyte 0x0000000000000021 0xAC
// writebyte 0x000000000000080A 0xFF
 */
int vmpc;
int pmpc;
int page_size;
uint64_t *diskAddresses;
uint64_t *shadowAddresses;
int *isDirty;

int disk_page_count = 100;
int current_disk_page = 0;
int current_physical_page = 0;


uint64_t* virtualAddresses;
uint64_t* physicalAddresses;


void accessMemory(int status) {
    uint64_t val = 0;
    uint64_t loc;

    if (!status) {
        scanf("%lx", &loc);
    } else {
        scanf("%lx %lx", &loc, &val);
    }

    if (loc >= vmpc * page_size) {
        printf(status ? "writebyte: segmentation fault\n" : "readbyte: segmentation fault\n");
        return;
    }

    uint64_t vpn = loc / page_size;

    if(physicalAddresses[vpn]==0){
        uint64_t ppn = physicalAddresses[vpn];
        posix_memalign((void **)&ppn, page_size, page_size);
        memset((void *)ppn, 0, page_size);
        virtualAddresses[vpn] = vpn * page_size;
        physicalAddresses[vpn] = ppn;
        printf("created physical page at 0x%016lX, "
               "mapped to virtual page at 0x%016lX\n",
               ppn,
               virtualAddresses[vpn]);
    }

    uint64_t physicalAddress = physicalAddresses[vpn] + (loc % page_size);

    if (!status) {
        printf("readbyte: VM location 0x%016lX, which is PM location 0x%016lX, contains value 0x%02X\n", loc, physicalAddress, *((uint8_t *)physicalAddress));
    } else {
        *((uint8_t *)physicalAddress) = (uint8_t)val;
        printf("writebyte: VM location 0x%016lX, which is PM location 0x%016lX, now contains value 0x%02lX\n", loc, physicalAddress, val);
    }
}
void evict_physical_page(uint64_t vpn) {
    uint64_t ppn = physicalAddresses[vpn];
    uint64_t shadow = shadowAddresses[vpn];

    if (isDirty[vpn]) {
        memcpy((void *)diskAddresses[shadow], (void *)ppn, page_size);
        printf("physical page at 0x%016lX, which corresponds to virtual page 0x%016lX, is evicted and dirty. Copied to disc at 0x%016lX and removed from physical memory.\n", ppn, virtualAddresses[vpn], diskAddresses[shadow]);
    } else {
        printf("physical page at 0x%016lX, which corresponds to virtual page 0x%016lX, is evicted and not dirty. Removed from physical memory.\n", ppn, virtualAddresses[vpn]);
    }

    free((void *)ppn);
    physicalAddresses[vpn] = 0;
}


int main(int argc, char *argv[]) {
    if (argc != 4) {
        fprintf(stderr, "usage: mmusim [pagesize] [vmpc] [pmpc]\n");
        return 1;
    }

    page_size = atoi(argv[1]);
    if ((page_size <= 0) || ((page_size & (page_size - 1)) != 0)) {
        printf("error: pagesize must be power of 2\n");
        exit(EXIT_FAILURE);
    }

    vmpc = atoi(argv[2]);
    pmpc = atoi(argv[3]);
    if (pmpc < vmpc) {
        fprintf(stderr, "error: pmpc must be larger than or equal to vmpc\n");
        return 1;
    }


    virtualAddresses = malloc(vmpc * sizeof(uint64_t));
    physicalAddresses = malloc(vmpc * sizeof(uint64_t));

    memset(virtualAddresses, 0, vmpc * sizeof(uint64_t));
    memset(physicalAddresses, 0, vmpc * sizeof(uint64_t));
    // Initialize disk
    diskAddresses = malloc(disk_page_count * sizeof(uint64_t));
    for (int i = 0; i < disk_page_count; ++i) {
        uint64_t disk_page;
        posix_memalign((void **)&disk_page, page_size, page_size);
        memset((void *)disk_page, 0, page_size);
        diskAddresses[i] = disk_page;
    }

    shadowAddresses = malloc(vmpc * sizeof(uint64_t));
    isDirty = malloc(vmpc * sizeof(int));

    memset(shadowAddresses, 0, vmpc * sizeof(uint64_t));
    memset(isDirty, 0, vmpc * sizeof(int));

    char cmd[10000];
    while (1) {
        printf("> ");
        scanf("%s", cmd);
        if (strcmp(cmd, "exit") == 0) {
            break;
        }
        if (strcmp(cmd, "readbyte") == 0) {
            accessMemory(0);
            continue;
        }
        if (strcmp(cmd, "writebyte") == 0) {
            accessMemory(1);
            continue;
        }
    }
    for (int i = 0; i < vmpc; ++i) {
        if (physicalAddresses[i] != 0) {
            printf("physical page at 0x%lX destroyed\n",
                   physicalAddresses[i]);
            free((void *)physicalAddresses[i]);
        }
    }
    free(virtualAddresses);
    free(physicalAddresses);
    return 0;
}
