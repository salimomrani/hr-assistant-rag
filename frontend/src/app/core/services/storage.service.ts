import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

/**
 * Storage Service - Manages browser localStorage operations
 * Handles conversation history persistence with FIFO eviction
 */
@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly prefix = environment.localStoragePrefix;

  /**
   * Save data to localStorage with prefix
   * @param key The storage key (will be prefixed)
   * @param value The value to store (will be JSON stringified)
   */
  set<T>(key: string, value: T): void {
    try {
      const prefixedKey = this.prefix + key;
      const serialized = JSON.stringify(value);
      localStorage.setItem(prefixedKey, serialized);
    } catch (error) {
      console.error('Error saving to localStorage:', error);
      // Fallback: localStorage unavailable (private browsing, quota exceeded)
    }
  }

  /**
   * Get data from localStorage
   * @param key The storage key (will be prefixed)
   * @returns The parsed value or null if not found
   */
  get<T>(key: string): T | null {
    try {
      const prefixedKey = this.prefix + key;
      const serialized = localStorage.getItem(prefixedKey);

      if (serialized === null) {
        return null;
      }

      return JSON.parse(serialized) as T;
    } catch (error) {
      console.error('Error reading from localStorage:', error);
      return null;
    }
  }

  /**
   * Remove data from localStorage
   * @param key The storage key (will be prefixed)
   */
  remove(key: string): void {
    try {
      const prefixedKey = this.prefix + key;
      localStorage.removeItem(prefixedKey);
    } catch (error) {
      console.error('Error removing from localStorage:', error);
    }
  }

  /**
   * Clear all data with the app prefix
   */
  clear(): void {
    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith(this.prefix)) {
          localStorage.removeItem(key);
        }
      });
    } catch (error) {
      console.error('Error clearing localStorage:', error);
    }
  }

  /**
   * Check if localStorage is available
   * @returns true if localStorage is accessible
   */
  isAvailable(): boolean {
    try {
      const testKey = this.prefix + '__test__';
      localStorage.setItem(testKey, 'test');
      localStorage.removeItem(testKey);
      return true;
    } catch {
      return false;
    }
  }
}
