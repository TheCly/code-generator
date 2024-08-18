package com.codegen;

import com.hnc.socialization.hennengcai.HncFormAliasDao;
import lombok.Data;

/**
 * 默认名称名称上下文，便于其他引入类方便使用
 */
@Data
public class DefaultNameContext {

  //vo就是entity
  private String voPackageName;

  private String voClassName;

  private String originalClassName;

  private String originalPackageName;
  //这些request的返回值，暂时不设置
//  private String queryPackageName;
//
//  private String queryClassName;
//
//  private String updaterPackageName;
//
//  private String updaterClassName;
//
//  private String creatorPackageName;
//
//  private String creatorClassName;
  private String daoPackageName;

  private String daoClassName;
  private String hncDaoPackageName="HncFormAliasDao";
  private String hncDaoClassName="HncFormAliasDao";

//  private String mapperPackageName;
//
//  private String mapperClassName;

//  private String repositoryPackageName;
//
//  private String repositoryClassName;

  private String servicePackageName;

  private String serviceClassName;

  private String serviceImplPackageName;

  private String serviceImplClassName;

  private String controllerPackageName;

  private String controllerClassName;

  /**
   * API 相关，都是给前端的返回值
   */
//  private String createPackageName;
//
//  private String createClassName;
//
//  private String updatePackageName;
//
//  private String updateClassName;
//
//  private String queryRequestPackageName;
//
//  private String queryRequestClassName;

//  private String responsePackageName;
//
//  private String responseClassName;

//  private String feignPackageName;
//
//  private String feignClassName;

}