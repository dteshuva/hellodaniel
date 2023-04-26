#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>

// readbyte 0xFF00AA00BB00CC00
// readbyte 0x0000000000000020
//readbyte 0x0000000000000021
// writebyte 0x0000000000000021 0xAC
// readbyte 0x0000000000000021
// writebyte 0x0000000000000021 0xAC
// writebyte 0x000000000000080A 0xFF
// readbyte 0x0000000000000021
// readbyte 0x000000000000080A

int page_size;
int vmpc;
int pmpc;

// Struct for Disc 
typedef struct {
    uint64_t addr;
    int is_dirty;
} Disk;

// Struct for pages 
typedef struct {
    uint64_t virtual_addr;
    uint64_t physical_addr;
    Disk *disk_page;
} Page;


int main(int argc, char *argv[]) {


    page_size = atoi(argv[1]);
    if ((page_size <= 0) || ((page_size & (page_size - 1)) != 0)) {
        printf("error: pagesize must be power of 2\n");
        exit(EXIT_FAILURE);
    }

    vmpc = atoi(argv[2]);
    pmpc = atoi(argv[3]);

    Page *page_table = malloc(vmpc * sizeof(Page));
    memset(page_table, 0, vmpc * sizeof(Page));

    // if (pmpc < vmpc) {
    //     fprintf(stderr, "error: pmpc must be larger than or equal to vmpc\n");
    //     return 1;
    // }

    Disk *disk = malloc(100 * sizeof(Disk));
    for (int i = 0; i < 100; ++i) {
        posix_memalign((void **)&disk[i].addr, page_size, page_size);
        disk[i].is_dirty = 0;
    }


    uint64_t *queue = malloc(pmpc * sizeof(uint64_t));
    int front_queue = 0, back_queue = 0;

    char cmd[1000];
    uint64_t loc, value;

    while (1) {
        printf(">");
        scanf("%s", cmd);

         if (strcmp(cmd, "readbyte") == 0) {
            scanf("%lx", &loc);
            if (loc >= vmpc * page_size) {
                printf("readbyte: segmentation fault\n");
            } else {
                uint64_t virtual_page = loc / page_size;
                uint64_t page_offset = loc % page_size;
                uint64_t physical_page = page_table[virtual_page].physical_addr;
                uint64_t physical_addr;
                 // printf("Virtual page: %PRIu64\n", virtual_page);
                if (physical_page == 0) {
                 //   printf("makes it here\n");
                    if (back_queue - front_queue >= pmpc) {
                        uint64_t ind = queue[front_queue% pmpc];
                        Page *ptr = &page_table[ind];
                        printf("physical page at 0x%016lX, which corresponds to virtual page at 0x%016lX, ",
                               ptr->physical_addr, ptr->virtual_addr);

                        if (ptr->disk_page->is_dirty) {
                            printf("is evicted and dirty. Copied to disk at 0x%016lX and removed from physical memory\n",
                                   ptr->disk_page->addr);
                            memcpy((void *)ptr->disk_page->addr,
                                   (void *)ptr->physical_addr, page_size);
                            ptr->disk_page->is_dirty = 0;
                        } else {
                            printf("is evicted and not dirty.\n");
                            printf("Removed from physical memory\n");
                        }
                        free((void *)ptr->physical_addr);
                        ptr->physical_addr = 0;
                        front_queue++;
                    }

                    posix_memalign((void **)&physical_page, page_size, page_size);
                    if (page_table[virtual_page].disk_page == NULL) {
                        page_table[virtual_page].disk_page = &disk[virtual_page % 100];
                    }
                    memcpy((void *)physical_page, (void *)page_table[virtual_page].disk_page->addr, page_size);
                    page_table[virtual_page].virtual_addr = virtual_page * page_size;
                    page_table[virtual_page].physical_addr = physical_page;
                    printf("physical page at 0x%016lX mapped to virtual page at 0x%016lX\n",
                           physical_page, page_table[virtual_page].virtual_addr);

                    int ind= back_queue % pmpc;
                    queue[ind] = virtual_page;
                    back_queue++;
                }

                physical_addr = physical_page + page_offset;
                uint8_t val = *((uint8_t *)physical_addr);
                printf("readbyte: VM location 0x%016lX, which is PM location 0x%016lX, contains value 0x%02X\n",
                       loc, physical_addr, val);
            }
        }  // end of readbyte
        else if (strcmp(cmd, "writebyte") == 0) {
            scanf("%lx %lx", &loc, &value);
            if (loc >= vmpc * page_size) {
                printf("writebyte: segmentation fault\n");
            } else {

                uint64_t virtual_page = loc / page_size;
                uint64_t page_offset = loc % page_size;
                uint64_t physical_page = page_table[virtual_page].physical_addr;
                uint64_t physical_addr;

                if (physical_page == 0) {
                    // Same eviction and allocation process as in readbyte
                    if (back_queue - front_queue >= pmpc) {
                        uint64_t ind = queue[front_queue % pmpc];
                        Page *ptr = &page_table[ind];
                        printf("physical page at 0x%016lX, which corresponds to virtual page at 0x%016lX, ",
                               ptr->physical_addr, ptr->virtual_addr);

                        if (ptr->disk_page->is_dirty) {
                            printf("is evicted and dirty. Copied to disk at 0x%016lX and removed from physical memory\n",
                                   ptr->disk_page->addr);
                            memcpy((void *)ptr->disk_page->addr,
                                   (void *)ptr->physical_addr, page_size);
                            ptr->disk_page->is_dirty = 0;
                        }

                        free((void *)ptr->physical_addr);
                        ptr->physical_addr = 0;
                        front_queue++;
                    }

                    posix_memalign((void **)&physical_page, page_size, page_size);
                    if (page_table[virtual_page].disk_page == NULL) {
                        page_table[virtual_page].disk_page = &disk[virtual_page % 100];
                    }
                                        memcpy((void *)physical_page, (void *)page_table[virtual_page].disk_page->addr, page_size);
                    page_table[virtual_page].virtual_addr = virtual_page * page_size;
                    page_table[virtual_page].physical_addr = physical_page;
                    printf("physical page at 0x%016lX mapped to virtual page at 0x%016lX\n",
                           physical_page, page_table[virtual_page].virtual_addr);
                    queue[back_queue % pmpc] = virtual_page;
                    back_queue++;
                }

                physical_addr = physical_page + page_offset;
                *((uint8_t *)physical_addr) = (uint8_t)value;
                page_table[virtual_page].disk_page->is_dirty = 1;
                printf("writebyte: VM location 0x%016lX, which is PM location 0x%016lX, now contains value 0x%02lX\n",
                       loc, physical_addr, value);
            }
        }
         else if (strcmp(cmd, "exit") == 0) {
             break;
         }
    }


    for (int i = 0; i < vmpc; ++i) {
        if (page_table[i].physical_addr != 0) {
            free((void *)page_table[i].physical_addr);
        }
    }
    free(page_table);
    for (int i = 0; i < 100; ++i) {
        free((void *)disk[i].addr);
    }
    free(disk);
    free(queue);

    return 0;
}


