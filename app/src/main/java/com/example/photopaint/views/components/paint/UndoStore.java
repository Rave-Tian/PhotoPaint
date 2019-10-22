package com.example.photopaint.views.components.paint;

import com.example.photopaint.helpers.AndroidUtilities;

import java.util.*;

public class UndoStore {

    public interface UndoStoreDelegate {
        void historyChanged();
    }

    private UndoStoreDelegate delegate;
    private Map<UUID, Runnable> uuidToOperationMap = new HashMap<>();
    private List<UUID> operations = new ArrayList<>();
    private Map<UUID, Runnable> uuidRecoverOperateMap = new HashMap<>();
    private List<UUID> recoverOperations = new Stack<>();

    public boolean canUndo() {
        return !operations.isEmpty();
    }

    public boolean canRecover(){
        return !recoverOperations.isEmpty();
    }

    public void setDelegate(UndoStoreDelegate undoStoreDelegate) {
        delegate = undoStoreDelegate;
    }

    public void registerUndo(UUID uuid, Runnable undoRunnable) {
        uuidToOperationMap.put(uuid, undoRunnable);
        operations.add(uuid);

        while (!recoverOperations.isEmpty()){
            int lastIndex = recoverOperations.size() - 1;
            UUID deleteId = recoverOperations.get(lastIndex);

            operations.remove(deleteId);
            uuidToOperationMap.remove(deleteId);

            recoverOperations.remove(lastIndex);
            uuidRecoverOperateMap.remove(deleteId);
        }

        notifyOfHistoryChanges();
    }

    public void unregisterUndo(UUID uuid) {
        uuidToOperationMap.remove(uuid);
        operations.remove(uuid);

        notifyOfHistoryChanges();
    }

    public void registerRecover(UUID uuid, Runnable recoverRunnable){
        uuidRecoverOperateMap.put(uuid, recoverRunnable);
    }

    public void unRegisterRecover(UUID uuid){
        uuidRecoverOperateMap.remove(uuid);
    }

    public void undo() {
        if (operations.size() == 0) {
            return;
        }

        int lastIndex = operations.size() - 1;
        UUID uuid = operations.get(lastIndex);
        Runnable undoRunnable = uuidToOperationMap.get(uuid);

        operations.remove(lastIndex);
        recoverOperations.add(uuid);

        undoRunnable.run();
        notifyOfHistoryChanges();
    }

    public void recover() {
        if (!canRecover()) {
            return;
        }

        int lastIndex = recoverOperations.size() - 1;
        UUID uuid = recoverOperations.get(lastIndex);
        recoverOperations.remove(lastIndex);
        Runnable recoverRunnable = uuidRecoverOperateMap.get(uuid);

        operations.add(uuid);

        recoverRunnable.run();
        notifyOfHistoryChanges();
    }

    public void reset() {
        // 清掉图层并清空撤销栈
        for (int index = operations.size() - 1; index >=0; index--){
            UUID uuid = operations.get(index);
            Runnable undoRunnable = uuidToOperationMap.get(uuid);
            uuidToOperationMap.remove(uuid);
            operations.remove(index);

            undoRunnable.run();
        }

        notifyOfHistoryChanges();
    }

    private void notifyOfHistoryChanges() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (delegate != null) {
                    delegate.historyChanged();
                }
            }
        });
    }
}
