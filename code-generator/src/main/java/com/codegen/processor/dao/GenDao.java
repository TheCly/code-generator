package com.codegen.processor.dao;

/**
 * @author superBig-cup 2024/8/18 上午11:26
 */
public @interface GenDao {
  String pkgName();

  String sourcePath() default "src/main/java";

  boolean overrideSource() default false;
}
