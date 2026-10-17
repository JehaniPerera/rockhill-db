package com.rockhill.reception_hall.menu;

public interface CatalogUsageChecker {

    boolean isMenuUsed(Integer menuId);

    boolean isPackageUsed(Integer packageId);
}
