package com.nfl.dm.shield.dynamic.config;

/**
 * In order to get even distribution of hash codes (avoiding linear performance O(n)), it is best to initialize to
 * a prime number.  As a hash table grows, one default algorithm increases size by 2n + 1.  Sun developers initially
 * chose 101 as the default number.  However, the growth characteristics of 101 are not as good as other selections.
 *
 * The book Java 2 Performance and Idiom Guide on page 73 offers the following as optimal initial bucket size:
 * 89, 179, 359, 719, 1439, 2879, 11519, 23029, 737279, 1474559, 2949119.
 *
 * We have chosen 89 as the starting place for all hash tables.
 */
public final class HashConfig {
    public static final int DEFAULT_HASH_TABLE_SIZE = 89;
}
