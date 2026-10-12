// 1. Create the interface
package com.example.demo.common.sync.service;

import com.example.demo.common.sync.event.DbSyncEvent;

public interface ReplicaSyncService {
    void sync(DbSyncEvent syncEvent);
}