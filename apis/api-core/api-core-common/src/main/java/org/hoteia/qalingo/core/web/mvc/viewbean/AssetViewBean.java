/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.web.mvc.viewbean;

public class AssetViewBean extends AbstractViewBean {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -450913534322324076L;

	protected String name;
	protected String scope;
	protected String type;
	protected String description;
    protected String title;
    protected String alt;
	protected String path;
	protected String size;
	protected String fileSize;
	protected boolean isDefault;

    protected String relativeWebPath;
	protected String absoluteWebPath;

	protected String detailsUrl;
	protected String editUrl;
	protected String backUrl;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	public String getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public boolean isDefault() {
		return isDefault;
	}

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

	public String getRelativeWebPath() {
        return relativeWebPath;
    }
	
	public void setRelativeWebPath(String relativeWebPath) {
        this.relativeWebPath = relativeWebPath;
    }
	
	public String getAbsoluteWebPath() {
		return absoluteWebPath;
	}
	
	public void setAbsoluteWebPath(String absoluteWebPath) {
		this.absoluteWebPath = absoluteWebPath;
	}
	
	public String getDetailsUrl() {
		return detailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}

	public String getEditUrl() {
		return editUrl;
	}

	public void setEditUrl(String editUrl) {
		this.editUrl = editUrl;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

}