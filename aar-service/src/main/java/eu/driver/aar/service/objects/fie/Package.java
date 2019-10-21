package eu.driver.aar.service.objects.fie;

import java.util.HashMap;
import java.util.Map;

public class Package {
	
	private String packageId;
	private Map<String, Category> packageCategoryMap = new HashMap<String, Category>();

	public Package(String packageId) {
		this.packageId = packageId;
	}

	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

	public Map<String, Category> getPackageCategoryMap() {
		return packageCategoryMap;
	}

	public void setPackageCategoryMap(Map<String, Category> packageCategoryMap) {
		this.packageCategoryMap = packageCategoryMap;
	}
	
	public void addCategory(String categoryId, Category category) {
		this.packageCategoryMap.put(categoryId, category);
	}
	
	public Category getCategory(String categoryId) {
		Category category =  this.packageCategoryMap.get(categoryId);
		if (category == null) {
			category = new Category(categoryId);
			this.addCategory(categoryId, category);
		}
		return category;
	}
}
