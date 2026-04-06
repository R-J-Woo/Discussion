package com.discussion.ryu.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

public class CustomFunctionsContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions fc) {

        // 단일 컬럼용 (title or content)
        fc.getFunctionRegistry().registerPattern(
                "match_against_single",
                "MATCH (?1) AGAINST (?2 IN BOOLEAN MODE)",
                fc.getTypeConfiguration()
                        .getBasicTypeRegistry()
                        .resolve(StandardBasicTypes.DOUBLE)
        );

        // 복합 컬럼용 (title + content)
        fc.getFunctionRegistry().registerPattern(
                "match_against_multi",
                "MATCH (?1, ?2) AGAINST (?3 IN BOOLEAN MODE)",
                fc.getTypeConfiguration()
                        .getBasicTypeRegistry()
                        .resolve(StandardBasicTypes.DOUBLE)
        );
    }
}
