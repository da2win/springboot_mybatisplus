package io.z77z.shiro;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.z77z.entity.SysPermission;
import io.z77z.entity.SysRole;
import io.z77z.entity.SysUser;
import io.z77z.service.SysPermissionServiceImpl;
import io.z77z.service.SysRoleServiceImpl;
import io.z77z.service.SysUserServiceImpl;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * shiro身份校验核心类
 * 
 * @author 作者: z77z
 * @date 创建时间：2017年2月10日 下午3:19:48
 */

public class MyShiroRealm extends AuthorizingRealm {

	@Autowired
	private SysUserServiceImpl sysUserService;
	
	@Autowired
	private SysPermissionServiceImpl sysPermissionService;
	
	@Autowired
	private SysRoleServiceImpl sysRoleService;

	/**
	 * 认证信息.(身份验证) : Authentication 是用来验证用户身份
	 * 
	 * @param token
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authcToken) throws AuthenticationException {
		System.out.println("身份认证方法：MyShiroRealm.doGetAuthenticationInfo()");

		ShiroToken token = (ShiroToken) authcToken;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nickname", token.getUsername());
		map.put("pswd", token.getPswd());
		SysUser user = null;
		// 从数据库获取对应用户名密码的用户
		List<SysUser> userList = sysUserService.selectByMap(map);
		if(userList.size()!=0){
			user = userList.get(0);
		}
		if (null == user) {
			throw new AccountException("帐号或密码不正确！");
		}else if(user.getStatus()==0){
			/**
			 * 如果用户的status为禁用。那么就抛出<code>DisabledAccountException</code>
			 */
			throw new DisabledAccountException("帐号已经禁止登录！");
		}else{
			//更新登录时间 last login time
			user.setLastLoginTime(new Date());
			sysUserService.updateById(user);
		}
		return new SimpleAuthenticationInfo(user, user.getPswd(), getName());
	}

	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		System.out.println("权限认证方法：MyShiroRealm.doGetAuthorizationInfo()");
		SysUser token = (SysUser)SecurityUtils.getSubject().getPrincipal();
		String userId = token.getId();
		SimpleAuthorizationInfo info =  new SimpleAuthorizationInfo();
		//根据用户ID查询角色（role），放入到Authorization里。
		/*Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", userId);
		List<SysRole> roleList = sysRoleService.selectByMap(map);
		Set<String> roleSet = new HashSet<String>();
		for(SysRole role : roleList){
			roleSet.add(role.getType());
		}*/
		Set<String> roleSet = new HashSet<String>();
		roleSet.add("100002");
		info.setRoles(roleSet);
		//根据用户ID查询权限（permission），放入到Authorization里。
		/*List<SysPermission> permissionList = sysPermissionService.selectByMap(map);
		Set<String> permissionSet = new HashSet<String>();
		for(SysPermission Permission : permissionList){
			permissionSet.add(Permission.getName());
		}*/
		Set<String> permissionSet = new HashSet<String>();
		permissionSet.add("权限添加");
		info.setStringPermissions(permissionSet);
        return info;
	}

	/**
	 * 清空当前用户权限信息
	 */
	public void clearCachedAuthorizationInfo() {
		PrincipalCollection principalCollection = SecurityUtils.getSubject()
				.getPrincipals();
		SimplePrincipalCollection principals = new SimplePrincipalCollection(
				principalCollection, getName());
		super.clearCachedAuthorizationInfo(principals);
	}

	/**
	 * 指定principalCollection 清除
	 */
	public void clearCachedAuthorizationInfo(
			PrincipalCollection principalCollection) {
		SimplePrincipalCollection principals = new SimplePrincipalCollection(
				principalCollection, getName());
		super.clearCachedAuthorizationInfo(principals);
	}

}
