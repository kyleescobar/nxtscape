import de.fayard.refreshVersions.core.FeatureFlag.LIBS

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.40.1"
}

rootProject.name = "nxtscape"

/**
 * ===== CLIENT MODULES =====
 */

include(":client")
include(":client:natives")

/**
 * ===== SERVER MODULES =====
 */

include(":server")