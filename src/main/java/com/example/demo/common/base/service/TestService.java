package com.example.demo.common.base.service;

import com.example.demo.common.base.entity.TestEntity;
import com.example.demo.common.base.repository.TestRepository;

public class TestService extends AbstractBaseService<TestEntity, Long> {

    private boolean afterSaveCalled = false;
    private boolean afterDeleteCalled = false;

    public TestService(TestRepository repository) {
        super(repository);
    }

    @Override
    protected String getCacheName() {
        return "testEntities";
    }

    @Override
    protected Class<?> getEntityClass() {
        return TestEntity.class;
    }

    @Override
    protected void onAfterSave(TestEntity entity) {
        afterSaveCalled = true;
    }

    @Override
    protected void onAfterDelete(Long id) {
        afterDeleteCalled = true;
    }

    public boolean isAfterSaveCalled() { return afterSaveCalled; }
    public boolean isAfterDeleteCalled() { return afterDeleteCalled; }
}
