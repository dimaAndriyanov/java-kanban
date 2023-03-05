package kanban.service;

import org.junit.jupiter.api.BeforeEach;

class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager>{
    @BeforeEach
    public void setManager() {
        setManager(new InMemoryHistoryManager());
    }
}