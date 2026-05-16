/**
 * LocalStorage Utility for Data Persistence
 * Provides type-safe storage operations with error handling
 */

const STORAGE_KEY = 'jpmonitor_database';
const STORAGE_VERSION = '1.0';

export interface StorageData {
    version: string;
    timestamp: string;
    data: {
        employees: any[];
        suppliers: any[];
        locations: any[];
        pits: any[];
        equipment: any[];
        production: any[];
        dailyLogs: any[];
        stockpiles: any[];
        incidents: any[];
        maintenanceRecords: any[];
        spareParts: any[];
        inventoryTransactions: any[];
        unitMutations: any[];
        goodsShipments: any[];
        auditLogs: any[];
    };
}

/**
 * Save data to localStorage with error handling
 */
export function saveToStorage(data: StorageData['data']): boolean {
    try {
        const storageData: StorageData = {
            version: STORAGE_VERSION,
            timestamp: new Date().toISOString(),
            data
        };

        const jsonString = JSON.stringify(storageData);
        localStorage.setItem(STORAGE_KEY, jsonString);

        if (!import.meta.env.PROD) {
            console.log('✅ Data saved to localStorage', {
                size: (jsonString.length / 1024).toFixed(2) + ' KB',
                timestamp: storageData.timestamp
            });
        }

        return true;
    } catch {
        if (!import.meta.env.PROD) {
            console.error('❌ Failed to save to localStorage');
        }

        return false;
    }
}

/**
 * Load data from localStorage
 */
export function loadFromStorage(): StorageData['data'] | null {
    try {
        const jsonString = localStorage.getItem(STORAGE_KEY);

        if (!jsonString) {
            if (!import.meta.env.PROD) {
                console.log('ℹ️ No saved data found in localStorage');
            }
            return null;
        }

        const storageData: StorageData = JSON.parse(jsonString);

        // Version check (for future migrations)
        if (storageData.version !== STORAGE_VERSION) {
            if (!import.meta.env.PROD) {
                console.warn(`⚠️ Storage version mismatch. Expected ${STORAGE_VERSION}, got ${storageData.version}`);
            }
        }

        if (!import.meta.env.PROD) {
            console.log('✅ Data loaded from localStorage', {
                size: (jsonString.length / 1024).toFixed(2) + ' KB',
                timestamp: storageData.timestamp,
                age: getDataAge(storageData.timestamp)
            });
        }

        return storageData.data;

    } catch {
        if (!import.meta.env.PROD) {
            console.error('❌ Failed to load from localStorage');
        }
        return null;
    }
}

/**
 * Clear all stored data
 */
export function clearStorage(): void {
    try {
        localStorage.removeItem(STORAGE_KEY);
        if (!import.meta.env.PROD) {
            console.log('✅ Storage cleared');
        }
    } catch {
        if (!import.meta.env.PROD) {
            console.error('❌ Failed to clear storage');
        }
    }
}

/**
 * Get storage statistics
 */
export function getStorageStats(): {
    exists: boolean;
    size: string;
    timestamp?: string;
    age?: string;
} {
    try {
        const jsonString = localStorage.getItem(STORAGE_KEY);

        if (!jsonString) {
            return { exists: false, size: '0 KB' };
        }

        const storageData: StorageData = JSON.parse(jsonString);

        return {
            exists: true,
            size: (jsonString.length / 1024).toFixed(2) + ' KB',
            timestamp: storageData.timestamp,
            age: getDataAge(storageData.timestamp)
        };

    } catch {
        return { exists: false, size: '0 KB' };
    }
}

/**
 * Helper: Calculate data age
 */
function getDataAge(timestamp: string): string {
    const now = new Date();
    const saved = new Date(timestamp);
    const diffMs = now.getTime() - saved.getTime();

    const minutes = Math.floor(diffMs / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days} day${days > 1 ? 's' : ''} ago`;
    if (hours > 0) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    if (minutes > 0) return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    return 'just now';
}

/**
 * Debounced save function for performance optimization
 */
let saveTimeout: NodeJS.Timeout | null = null;

export function debouncedSave(data: StorageData['data'], delay: number = 1000): void {
    if (saveTimeout) {
        clearTimeout(saveTimeout);
    }

    saveTimeout = setTimeout(() => {
        saveToStorage(data);
        saveTimeout = null;
    }, delay);
}
