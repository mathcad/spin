package org.spin.datasource.spring.boot.autoconfigure.druid;


import java.util.Objects;

/**
 * Druid防火墙配置
 *
 * @author TaoYu
 */
public class DruidWallConfig {


    private Boolean noneBaseStatementAllow;

    private Boolean callAllow;
    private Boolean selectAllow;
    private Boolean selectIntoAllow;
    private Boolean selectIntoOutfileAllow;
    private Boolean selectWhereAlwayTrueCheck;
    private Boolean selectHavingAlwayTrueCheck;
    private Boolean selectUnionCheck;
    private Boolean selectMinusCheck;
    private Boolean selectExceptCheck;
    private Boolean selectIntersectCheck;
    private Boolean createTableAllow;
    private Boolean dropTableAllow;
    private Boolean alterTableAllow;
    private Boolean renameTableAllow;
    private Boolean hintAllow;
    private Boolean lockTableAllow;
    private Boolean startTransactionAllow;
    private Boolean blockAllow;

    private Boolean conditionAndAlwayTrueAllow;
    private Boolean conditionAndAlwayFalseAllow;
    private Boolean conditionDoubleConstAllow;
    private Boolean conditionLikeTrueAllow;

    private Boolean selectAllColumnAllow;

    private Boolean deleteAllow;
    private Boolean deleteWhereAlwayTrueCheck;
    private Boolean deleteWhereNoneCheck;

    private Boolean updateAllow;
    private Boolean updateWhereAlayTrueCheck;
    private Boolean updateWhereNoneCheck;

    private Boolean insertAllow;
    private Boolean mergeAllow;
    private Boolean minusAllow;
    private Boolean intersectAllow;
    private Boolean replaceAllow;
    private Boolean setAllow;
    private Boolean commitAllow;
    private Boolean rollbackAllow;
    private Boolean useAllow;

    private Boolean multiStatementAllow;

    private Boolean truncateAllow;

    private Boolean commentAllow;
    private Boolean strictSyntaxCheck;
    private Boolean constArithmeticAllow;
    private Boolean limitZeroAllow;

    private Boolean describeAllow;
    private Boolean showAllow;

    private Boolean schemaCheck;
    private Boolean tableCheck;
    private Boolean functionCheck;
    private Boolean objectCheck;
    private Boolean variantCheck;

    private Boolean mustParameterized;

    private Boolean doPrivilegedAllow;

    private String dir;

    private String tenantTablePattern;
    private String tenantColumn;

    private Boolean wrapAllow;
    private Boolean metadataAllow;

    private Boolean conditionOpXorAllow;
    private Boolean conditionOpBitwseAllow;

    private Boolean caseConditionConstAllow;

    private Boolean completeInsertValuesCheck;
    private Integer insertValuesCheckSize;

    private Integer selectLimit;

    public Boolean getNoneBaseStatementAllow() {
        return noneBaseStatementAllow;
    }

    public void setNoneBaseStatementAllow(Boolean noneBaseStatementAllow) {
        this.noneBaseStatementAllow = noneBaseStatementAllow;
    }

    public Boolean getCallAllow() {
        return callAllow;
    }

    public void setCallAllow(Boolean callAllow) {
        this.callAllow = callAllow;
    }

    public Boolean getSelectAllow() {
        return selectAllow;
    }

    public void setSelectAllow(Boolean selectAllow) {
        this.selectAllow = selectAllow;
    }

    public Boolean getSelectIntoAllow() {
        return selectIntoAllow;
    }

    public void setSelectIntoAllow(Boolean selectIntoAllow) {
        this.selectIntoAllow = selectIntoAllow;
    }

    public Boolean getSelectIntoOutfileAllow() {
        return selectIntoOutfileAllow;
    }

    public void setSelectIntoOutfileAllow(Boolean selectIntoOutfileAllow) {
        this.selectIntoOutfileAllow = selectIntoOutfileAllow;
    }

    public Boolean getSelectWhereAlwayTrueCheck() {
        return selectWhereAlwayTrueCheck;
    }

    public void setSelectWhereAlwayTrueCheck(Boolean selectWhereAlwayTrueCheck) {
        this.selectWhereAlwayTrueCheck = selectWhereAlwayTrueCheck;
    }

    public Boolean getSelectHavingAlwayTrueCheck() {
        return selectHavingAlwayTrueCheck;
    }

    public void setSelectHavingAlwayTrueCheck(Boolean selectHavingAlwayTrueCheck) {
        this.selectHavingAlwayTrueCheck = selectHavingAlwayTrueCheck;
    }

    public Boolean getSelectUnionCheck() {
        return selectUnionCheck;
    }

    public void setSelectUnionCheck(Boolean selectUnionCheck) {
        this.selectUnionCheck = selectUnionCheck;
    }

    public Boolean getSelectMinusCheck() {
        return selectMinusCheck;
    }

    public void setSelectMinusCheck(Boolean selectMinusCheck) {
        this.selectMinusCheck = selectMinusCheck;
    }

    public Boolean getSelectExceptCheck() {
        return selectExceptCheck;
    }

    public void setSelectExceptCheck(Boolean selectExceptCheck) {
        this.selectExceptCheck = selectExceptCheck;
    }

    public Boolean getSelectIntersectCheck() {
        return selectIntersectCheck;
    }

    public void setSelectIntersectCheck(Boolean selectIntersectCheck) {
        this.selectIntersectCheck = selectIntersectCheck;
    }

    public Boolean getCreateTableAllow() {
        return createTableAllow;
    }

    public void setCreateTableAllow(Boolean createTableAllow) {
        this.createTableAllow = createTableAllow;
    }

    public Boolean getDropTableAllow() {
        return dropTableAllow;
    }

    public void setDropTableAllow(Boolean dropTableAllow) {
        this.dropTableAllow = dropTableAllow;
    }

    public Boolean getAlterTableAllow() {
        return alterTableAllow;
    }

    public void setAlterTableAllow(Boolean alterTableAllow) {
        this.alterTableAllow = alterTableAllow;
    }

    public Boolean getRenameTableAllow() {
        return renameTableAllow;
    }

    public void setRenameTableAllow(Boolean renameTableAllow) {
        this.renameTableAllow = renameTableAllow;
    }

    public Boolean getHintAllow() {
        return hintAllow;
    }

    public void setHintAllow(Boolean hintAllow) {
        this.hintAllow = hintAllow;
    }

    public Boolean getLockTableAllow() {
        return lockTableAllow;
    }

    public void setLockTableAllow(Boolean lockTableAllow) {
        this.lockTableAllow = lockTableAllow;
    }

    public Boolean getStartTransactionAllow() {
        return startTransactionAllow;
    }

    public void setStartTransactionAllow(Boolean startTransactionAllow) {
        this.startTransactionAllow = startTransactionAllow;
    }

    public Boolean getBlockAllow() {
        return blockAllow;
    }

    public void setBlockAllow(Boolean blockAllow) {
        this.blockAllow = blockAllow;
    }

    public Boolean getConditionAndAlwayTrueAllow() {
        return conditionAndAlwayTrueAllow;
    }

    public void setConditionAndAlwayTrueAllow(Boolean conditionAndAlwayTrueAllow) {
        this.conditionAndAlwayTrueAllow = conditionAndAlwayTrueAllow;
    }

    public Boolean getConditionAndAlwayFalseAllow() {
        return conditionAndAlwayFalseAllow;
    }

    public void setConditionAndAlwayFalseAllow(Boolean conditionAndAlwayFalseAllow) {
        this.conditionAndAlwayFalseAllow = conditionAndAlwayFalseAllow;
    }

    public Boolean getConditionDoubleConstAllow() {
        return conditionDoubleConstAllow;
    }

    public void setConditionDoubleConstAllow(Boolean conditionDoubleConstAllow) {
        this.conditionDoubleConstAllow = conditionDoubleConstAllow;
    }

    public Boolean getConditionLikeTrueAllow() {
        return conditionLikeTrueAllow;
    }

    public void setConditionLikeTrueAllow(Boolean conditionLikeTrueAllow) {
        this.conditionLikeTrueAllow = conditionLikeTrueAllow;
    }

    public Boolean getSelectAllColumnAllow() {
        return selectAllColumnAllow;
    }

    public void setSelectAllColumnAllow(Boolean selectAllColumnAllow) {
        this.selectAllColumnAllow = selectAllColumnAllow;
    }

    public Boolean getDeleteAllow() {
        return deleteAllow;
    }

    public void setDeleteAllow(Boolean deleteAllow) {
        this.deleteAllow = deleteAllow;
    }

    public Boolean getDeleteWhereAlwayTrueCheck() {
        return deleteWhereAlwayTrueCheck;
    }

    public void setDeleteWhereAlwayTrueCheck(Boolean deleteWhereAlwayTrueCheck) {
        this.deleteWhereAlwayTrueCheck = deleteWhereAlwayTrueCheck;
    }

    public Boolean getDeleteWhereNoneCheck() {
        return deleteWhereNoneCheck;
    }

    public void setDeleteWhereNoneCheck(Boolean deleteWhereNoneCheck) {
        this.deleteWhereNoneCheck = deleteWhereNoneCheck;
    }

    public Boolean getUpdateAllow() {
        return updateAllow;
    }

    public void setUpdateAllow(Boolean updateAllow) {
        this.updateAllow = updateAllow;
    }

    public Boolean getUpdateWhereAlayTrueCheck() {
        return updateWhereAlayTrueCheck;
    }

    public void setUpdateWhereAlayTrueCheck(Boolean updateWhereAlayTrueCheck) {
        this.updateWhereAlayTrueCheck = updateWhereAlayTrueCheck;
    }

    public Boolean getUpdateWhereNoneCheck() {
        return updateWhereNoneCheck;
    }

    public void setUpdateWhereNoneCheck(Boolean updateWhereNoneCheck) {
        this.updateWhereNoneCheck = updateWhereNoneCheck;
    }

    public Boolean getInsertAllow() {
        return insertAllow;
    }

    public void setInsertAllow(Boolean insertAllow) {
        this.insertAllow = insertAllow;
    }

    public Boolean getMergeAllow() {
        return mergeAllow;
    }

    public void setMergeAllow(Boolean mergeAllow) {
        this.mergeAllow = mergeAllow;
    }

    public Boolean getMinusAllow() {
        return minusAllow;
    }

    public void setMinusAllow(Boolean minusAllow) {
        this.minusAllow = minusAllow;
    }

    public Boolean getIntersectAllow() {
        return intersectAllow;
    }

    public void setIntersectAllow(Boolean intersectAllow) {
        this.intersectAllow = intersectAllow;
    }

    public Boolean getReplaceAllow() {
        return replaceAllow;
    }

    public void setReplaceAllow(Boolean replaceAllow) {
        this.replaceAllow = replaceAllow;
    }

    public Boolean getSetAllow() {
        return setAllow;
    }

    public void setSetAllow(Boolean setAllow) {
        this.setAllow = setAllow;
    }

    public Boolean getCommitAllow() {
        return commitAllow;
    }

    public void setCommitAllow(Boolean commitAllow) {
        this.commitAllow = commitAllow;
    }

    public Boolean getRollbackAllow() {
        return rollbackAllow;
    }

    public void setRollbackAllow(Boolean rollbackAllow) {
        this.rollbackAllow = rollbackAllow;
    }

    public Boolean getUseAllow() {
        return useAllow;
    }

    public void setUseAllow(Boolean useAllow) {
        this.useAllow = useAllow;
    }

    public Boolean getMultiStatementAllow() {
        return multiStatementAllow;
    }

    public void setMultiStatementAllow(Boolean multiStatementAllow) {
        this.multiStatementAllow = multiStatementAllow;
    }

    public Boolean getTruncateAllow() {
        return truncateAllow;
    }

    public void setTruncateAllow(Boolean truncateAllow) {
        this.truncateAllow = truncateAllow;
    }

    public Boolean getCommentAllow() {
        return commentAllow;
    }

    public void setCommentAllow(Boolean commentAllow) {
        this.commentAllow = commentAllow;
    }

    public Boolean getStrictSyntaxCheck() {
        return strictSyntaxCheck;
    }

    public void setStrictSyntaxCheck(Boolean strictSyntaxCheck) {
        this.strictSyntaxCheck = strictSyntaxCheck;
    }

    public Boolean getConstArithmeticAllow() {
        return constArithmeticAllow;
    }

    public void setConstArithmeticAllow(Boolean constArithmeticAllow) {
        this.constArithmeticAllow = constArithmeticAllow;
    }

    public Boolean getLimitZeroAllow() {
        return limitZeroAllow;
    }

    public void setLimitZeroAllow(Boolean limitZeroAllow) {
        this.limitZeroAllow = limitZeroAllow;
    }

    public Boolean getDescribeAllow() {
        return describeAllow;
    }

    public void setDescribeAllow(Boolean describeAllow) {
        this.describeAllow = describeAllow;
    }

    public Boolean getShowAllow() {
        return showAllow;
    }

    public void setShowAllow(Boolean showAllow) {
        this.showAllow = showAllow;
    }

    public Boolean getSchemaCheck() {
        return schemaCheck;
    }

    public void setSchemaCheck(Boolean schemaCheck) {
        this.schemaCheck = schemaCheck;
    }

    public Boolean getTableCheck() {
        return tableCheck;
    }

    public void setTableCheck(Boolean tableCheck) {
        this.tableCheck = tableCheck;
    }

    public Boolean getFunctionCheck() {
        return functionCheck;
    }

    public void setFunctionCheck(Boolean functionCheck) {
        this.functionCheck = functionCheck;
    }

    public Boolean getObjectCheck() {
        return objectCheck;
    }

    public void setObjectCheck(Boolean objectCheck) {
        this.objectCheck = objectCheck;
    }

    public Boolean getVariantCheck() {
        return variantCheck;
    }

    public void setVariantCheck(Boolean variantCheck) {
        this.variantCheck = variantCheck;
    }

    public Boolean getMustParameterized() {
        return mustParameterized;
    }

    public void setMustParameterized(Boolean mustParameterized) {
        this.mustParameterized = mustParameterized;
    }

    public Boolean getDoPrivilegedAllow() {
        return doPrivilegedAllow;
    }

    public void setDoPrivilegedAllow(Boolean doPrivilegedAllow) {
        this.doPrivilegedAllow = doPrivilegedAllow;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getTenantTablePattern() {
        return tenantTablePattern;
    }

    public void setTenantTablePattern(String tenantTablePattern) {
        this.tenantTablePattern = tenantTablePattern;
    }

    public String getTenantColumn() {
        return tenantColumn;
    }

    public void setTenantColumn(String tenantColumn) {
        this.tenantColumn = tenantColumn;
    }

    public Boolean getWrapAllow() {
        return wrapAllow;
    }

    public void setWrapAllow(Boolean wrapAllow) {
        this.wrapAllow = wrapAllow;
    }

    public Boolean getMetadataAllow() {
        return metadataAllow;
    }

    public void setMetadataAllow(Boolean metadataAllow) {
        this.metadataAllow = metadataAllow;
    }

    public Boolean getConditionOpXorAllow() {
        return conditionOpXorAllow;
    }

    public void setConditionOpXorAllow(Boolean conditionOpXorAllow) {
        this.conditionOpXorAllow = conditionOpXorAllow;
    }

    public Boolean getConditionOpBitwseAllow() {
        return conditionOpBitwseAllow;
    }

    public void setConditionOpBitwseAllow(Boolean conditionOpBitwseAllow) {
        this.conditionOpBitwseAllow = conditionOpBitwseAllow;
    }

    public Boolean getCaseConditionConstAllow() {
        return caseConditionConstAllow;
    }

    public void setCaseConditionConstAllow(Boolean caseConditionConstAllow) {
        this.caseConditionConstAllow = caseConditionConstAllow;
    }

    public Boolean getCompleteInsertValuesCheck() {
        return completeInsertValuesCheck;
    }

    public void setCompleteInsertValuesCheck(Boolean completeInsertValuesCheck) {
        this.completeInsertValuesCheck = completeInsertValuesCheck;
    }

    public Integer getInsertValuesCheckSize() {
        return insertValuesCheckSize;
    }

    public void setInsertValuesCheckSize(Integer insertValuesCheckSize) {
        this.insertValuesCheckSize = insertValuesCheckSize;
    }

    public Integer getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(Integer selectLimit) {
        this.selectLimit = selectLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DruidWallConfig)) return false;
        DruidWallConfig that = (DruidWallConfig) o;
        return Objects.equals(noneBaseStatementAllow, that.noneBaseStatementAllow) && Objects.equals(callAllow, that.callAllow)
            && Objects.equals(selectAllow, that.selectAllow) && Objects.equals(selectIntoAllow, that.selectIntoAllow)
            && Objects.equals(selectIntoOutfileAllow, that.selectIntoOutfileAllow)

            && Objects.equals(selectWhereAlwayTrueCheck, that.selectWhereAlwayTrueCheck)
            && Objects.equals(selectHavingAlwayTrueCheck, that.selectHavingAlwayTrueCheck) && Objects.equals(selectUnionCheck, that.selectUnionCheck)
            && Objects.equals(selectMinusCheck, that.selectMinusCheck) && Objects.equals(selectExceptCheck, that.selectExceptCheck)
            && Objects.equals(selectIntersectCheck, that.selectIntersectCheck) && Objects.equals(createTableAllow, that.createTableAllow)
            && Objects.equals(dropTableAllow, that.dropTableAllow) && Objects.equals(alterTableAllow, that.alterTableAllow)
            && Objects.equals(renameTableAllow, that.renameTableAllow) && Objects.equals(hintAllow, that.hintAllow)
            && Objects.equals(lockTableAllow, that.lockTableAllow) && Objects.equals(startTransactionAllow, that.startTransactionAllow)
            && Objects.equals(blockAllow, that.blockAllow) && Objects.equals(conditionAndAlwayTrueAllow, that.conditionAndAlwayTrueAllow)
            && Objects.equals(conditionAndAlwayFalseAllow, that.conditionAndAlwayFalseAllow)
            && Objects.equals(conditionDoubleConstAllow, that.conditionDoubleConstAllow)
            && Objects.equals(conditionLikeTrueAllow, that.conditionLikeTrueAllow) && Objects.equals(selectAllColumnAllow, that.selectAllColumnAllow)
            && Objects.equals(deleteAllow, that.deleteAllow) && Objects.equals(deleteWhereAlwayTrueCheck, that.deleteWhereAlwayTrueCheck)
            && Objects.equals(deleteWhereNoneCheck, that.deleteWhereNoneCheck) && Objects.equals(updateAllow, that.updateAllow)
            && Objects.equals(updateWhereAlayTrueCheck, that.updateWhereAlayTrueCheck)
            && Objects.equals(updateWhereNoneCheck, that.updateWhereNoneCheck)
            && Objects.equals(insertAllow, that.insertAllow) && Objects.equals(mergeAllow, that.mergeAllow)
            && Objects.equals(minusAllow, that.minusAllow)
            && Objects.equals(intersectAllow, that.intersectAllow) && Objects.equals(replaceAllow, that.replaceAllow)
            && Objects.equals(setAllow, that.setAllow) && Objects.equals(commitAllow, that.commitAllow)
            && Objects.equals(rollbackAllow, that.rollbackAllow) && Objects.equals(useAllow, that.useAllow)
            && Objects.equals(multiStatementAllow, that.multiStatementAllow) && Objects.equals(truncateAllow, that.truncateAllow)
            && Objects.equals(commentAllow, that.commentAllow) && Objects.equals(strictSyntaxCheck, that.strictSyntaxCheck)
            && Objects.equals(constArithmeticAllow, that.constArithmeticAllow) && Objects.equals(limitZeroAllow, that.limitZeroAllow)
            && Objects.equals(describeAllow, that.describeAllow) && Objects.equals(showAllow, that.showAllow)
            && Objects.equals(schemaCheck, that.schemaCheck) && Objects.equals(tableCheck, that.tableCheck)
            && Objects.equals(functionCheck, that.functionCheck) && Objects.equals(objectCheck, that.objectCheck)
            && Objects.equals(variantCheck, that.variantCheck) && Objects.equals(mustParameterized, that.mustParameterized)
            && Objects.equals(doPrivilegedAllow, that.doPrivilegedAllow) && Objects.equals(dir, that.dir)
            && Objects.equals(tenantTablePattern, that.tenantTablePattern) && Objects.equals(tenantColumn, that.tenantColumn)
            && Objects.equals(wrapAllow, that.wrapAllow) && Objects.equals(metadataAllow, that.metadataAllow)
            && Objects.equals(conditionOpXorAllow, that.conditionOpXorAllow) && Objects.equals(conditionOpBitwseAllow, that.conditionOpBitwseAllow)
            && Objects.equals(caseConditionConstAllow, that.caseConditionConstAllow)
            && Objects.equals(completeInsertValuesCheck, that.completeInsertValuesCheck)
            && Objects.equals(insertValuesCheckSize, that.insertValuesCheckSize) && Objects.equals(selectLimit, that.selectLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noneBaseStatementAllow, callAllow, selectAllow, selectIntoAllow, selectIntoOutfileAllow,
            selectWhereAlwayTrueCheck, selectHavingAlwayTrueCheck, selectUnionCheck, selectMinusCheck, selectExceptCheck,
            selectIntersectCheck, createTableAllow, dropTableAllow, alterTableAllow, renameTableAllow, hintAllow, lockTableAllow,
            startTransactionAllow, blockAllow, conditionAndAlwayTrueAllow, conditionAndAlwayFalseAllow, conditionDoubleConstAllow,
            conditionLikeTrueAllow, selectAllColumnAllow, deleteAllow, deleteWhereAlwayTrueCheck, deleteWhereNoneCheck, updateAllow,
            updateWhereAlayTrueCheck, updateWhereNoneCheck, insertAllow, mergeAllow, minusAllow, intersectAllow, replaceAllow, setAllow,
            commitAllow, rollbackAllow, useAllow, multiStatementAllow, truncateAllow, commentAllow, strictSyntaxCheck, constArithmeticAllow,
            limitZeroAllow, describeAllow, showAllow, schemaCheck, tableCheck, functionCheck, objectCheck, variantCheck, mustParameterized,
            doPrivilegedAllow, dir, tenantTablePattern, tenantColumn, wrapAllow, metadataAllow, conditionOpXorAllow, conditionOpBitwseAllow,
            caseConditionConstAllow, completeInsertValuesCheck, insertValuesCheckSize, selectLimit);
    }

    @Override
    public String toString() {
        return "DruidWallConfig{" +
            "noneBaseStatementAllow=" + noneBaseStatementAllow +
            ", callAllow=" + callAllow +
            ", selectAllow=" + selectAllow +
            ", selectIntoAllow=" + selectIntoAllow +
            ", selectIntoOutfileAllow=" + selectIntoOutfileAllow +
            ", selectWhereAlwayTrueCheck=" + selectWhereAlwayTrueCheck +
            ", selectHavingAlwayTrueCheck=" + selectHavingAlwayTrueCheck +
            ", selectUnionCheck=" + selectUnionCheck +
            ", selectMinusCheck=" + selectMinusCheck +
            ", selectExceptCheck=" + selectExceptCheck +
            ", selectIntersectCheck=" + selectIntersectCheck +
            ", createTableAllow=" + createTableAllow +
            ", dropTableAllow=" + dropTableAllow +
            ", alterTableAllow=" + alterTableAllow +
            ", renameTableAllow=" + renameTableAllow +
            ", hintAllow=" + hintAllow +
            ", lockTableAllow=" + lockTableAllow +
            ", startTransactionAllow=" + startTransactionAllow +
            ", blockAllow=" + blockAllow +
            ", conditionAndAlwayTrueAllow=" + conditionAndAlwayTrueAllow +
            ", conditionAndAlwayFalseAllow=" + conditionAndAlwayFalseAllow +
            ", conditionDoubleConstAllow=" + conditionDoubleConstAllow +
            ", conditionLikeTrueAllow=" + conditionLikeTrueAllow +
            ", selectAllColumnAllow=" + selectAllColumnAllow +
            ", deleteAllow=" + deleteAllow +
            ", deleteWhereAlwayTrueCheck=" + deleteWhereAlwayTrueCheck +
            ", deleteWhereNoneCheck=" + deleteWhereNoneCheck +
            ", updateAllow=" + updateAllow +
            ", updateWhereAlayTrueCheck=" + updateWhereAlayTrueCheck +
            ", updateWhereNoneCheck=" + updateWhereNoneCheck +
            ", insertAllow=" + insertAllow +
            ", mergeAllow=" + mergeAllow +
            ", minusAllow=" + minusAllow +
            ", intersectAllow=" + intersectAllow +
            ", replaceAllow=" + replaceAllow +
            ", setAllow=" + setAllow +
            ", commitAllow=" + commitAllow +
            ", rollbackAllow=" + rollbackAllow +
            ", useAllow=" + useAllow +
            ", multiStatementAllow=" + multiStatementAllow +
            ", truncateAllow=" + truncateAllow +
            ", commentAllow=" + commentAllow +
            ", strictSyntaxCheck=" + strictSyntaxCheck +
            ", constArithmeticAllow=" + constArithmeticAllow +
            ", limitZeroAllow=" + limitZeroAllow +
            ", describeAllow=" + describeAllow +
            ", showAllow=" + showAllow +
            ", schemaCheck=" + schemaCheck +
            ", tableCheck=" + tableCheck +
            ", functionCheck=" + functionCheck +
            ", objectCheck=" + objectCheck +
            ", variantCheck=" + variantCheck +
            ", mustParameterized=" + mustParameterized +
            ", doPrivilegedAllow=" + doPrivilegedAllow +
            ", dir='" + dir + '\'' +
            ", tenantTablePattern='" + tenantTablePattern + '\'' +
            ", tenantColumn='" + tenantColumn + '\'' +
            ", wrapAllow=" + wrapAllow +
            ", metadataAllow=" + metadataAllow +
            ", conditionOpXorAllow=" + conditionOpXorAllow +
            ", conditionOpBitwseAllow=" + conditionOpBitwseAllow +
            ", caseConditionConstAllow=" + caseConditionConstAllow +
            ", completeInsertValuesCheck=" + completeInsertValuesCheck +
            ", insertValuesCheckSize=" + insertValuesCheckSize +
            ", selectLimit=" + selectLimit +
            '}';
    }
}
