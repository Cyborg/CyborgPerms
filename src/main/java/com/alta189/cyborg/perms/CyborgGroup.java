package com.alta189.cyborg.perms;

import com.alta189.simplesave.Field;
import com.alta189.simplesave.Id;
import com.alta189.simplesave.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Table(name = "CyborgPermsGroup")
public class CyborgGroup {
	@Id
	private int id;
	@Field
	private String name;
	@Field
	private boolean wildcardPerm;
	@Field
	private String rawNegatedPerms;
	@Field
	private String rawPerms;
	// Non-persistence data
	private final List<String> negatedperms = new ArrayList<String>();
	private final List<String> perms = new ArrayList<String>();

	protected void load() {
		if (rawNegatedPerms != null) {
			Collections.addAll(negatedperms, rawNegatedPerms.split(";"));
		}

		if (rawPerms != null) {
			Collections.addAll(perms, rawPerms.split(";"));
		}
	}

	protected void flush() {
		StringBuilder builder = new StringBuilder();
		for (String perm : negatedperms) {
			builder.append(perm).append(";");
		}
		rawNegatedPerms = builder.toString();

		builder = new StringBuilder();
		for (String perm : perms) {
			builder.append(perm).append(";");
		}
		rawPerms = builder.toString();
	}

	public boolean hasPerm(String perm) {
		return hasPerm(perm, false);
	}

	public boolean hasPerm(String perm, boolean ignoreWildcard) {
		perm = perm.toLowerCase();
		if (hasNegatedPerm(perm)) {
			return false;
		}

		if (!ignoreWildcard && wildcardPerm) {
			return true;
		}

		return perms.contains(perm);
	}

	public boolean hasNegatedPerm(String perm) {
		return negatedperms.contains(perm.toLowerCase());
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasWildcardPerm() {
		return wildcardPerm;
	}

	public void setWildcardPerm(boolean wildcardPerm) {
		this.wildcardPerm = wildcardPerm;
	}

	public void addPerm(String perm) {
		perms.add(perm.toLowerCase());
	}

	public void removePerm(String perm) {
		perms.remove(perm.toLowerCase());
	}

	public List<String> getPerms() {
		return perms;
	}

	public void addNegatedPerm(String perm) {
		negatedperms.add(perm.toLowerCase());
	}

	public void removeNegatedPerm(String perm) {
		perms.remove(perm.toLowerCase());
	}

	public List<String> getNegatedPerms() {
		return negatedperms;
	}
}
