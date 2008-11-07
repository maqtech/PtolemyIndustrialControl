/*
 * Copyright (c) 2001-2003 Swedish Institute of Computer Science.
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 * This file is part of the lwIP TCP/IP stack.
 * 
 * Author: Adam Dunkels <adam@sics.se>
 *
 */
#include "lwip/opt.h"
#include "lwip/def.h"
#include "lwip/mem.h"
#include "fs.h"
#include "fsdata.h"
#include "fsdata.c"

/*-----------------------------------------------------------------------------------*/


/*-----------------------------------------------------------------------------------*/
struct fs_file *
fs_open(char *name)
{
  struct fs_file *file;
  struct fsdata_file_noconst *f;

  file = mem_malloc(sizeof(struct fs_file));
  if(file == NULL) {
    return NULL;
  }

  for(f = (struct fsdata_file_noconst *)FS_ROOT;
      f != NULL;
      f = (struct fsdata_file_noconst *)f->next) {
    if (!strcmp(name, (char *)f->name)) {
      file->data = (char *)f->data;
      file->len = f->len;
      file->index = f->len;
      file->pextension = NULL;
      return file;
    }
  }
  mem_free(file);
  return NULL;
}

/*-----------------------------------------------------------------------------------*/
void
fs_close(struct fs_file *file)
{
  mem_free(file);
}
/*-----------------------------------------------------------------------------------*/
int
fs_read(struct fs_file *file, char *buffer, int count)
{
  int read;

  if(file->index == file->len) {
    return -1;
  }

  read = file->len - file->index;
  if(read > count) {
    read = count;
  }

  memcpy(buffer, (file->data + file->index), read);
  file->index += read;

  return(read);
}

