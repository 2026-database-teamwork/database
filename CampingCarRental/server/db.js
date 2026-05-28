import oracledb from 'oracledb';
import dotenv from 'dotenv';

dotenv.config();

// In node-oracledb v6+, Thin mode is the default and does not require Oracle Instant Client libraries.
// We do not call initOracleClient() so that it runs in Thin mode.

let pool;

export async function initializeDb() {
  try {
    pool = await oracledb.createPool({
      user: process.env.DB_USER || 'system',
      password: process.env.DB_PASSWORD || 'oracle',
      connectString: process.env.DB_CONNECTION_STRING || 'localhost:1521/xe',
      poolMin: 2,
      poolMax: 10,
      poolIncrement: 1,
    });
    console.log('Oracle DB Connection Pool initialized successfully.');
  } catch (err) {
    console.error('================================================================');
    console.error('CRITICAL: Failed to initialize Oracle DB connection pool.');
    console.error('Please check if:');
    console.error('1. Oracle Database is running.');
    console.error('2. Connection details in "server/.env" are correct.');
    console.error('3. The port 1521 and the Service Name/SID (e.g. xe) are correct.');
    console.error('Error details:', err.message);
    console.error('================================================================');
    throw err;
  }
}

/**
 * Execute a SQL query
 * @param {string} sql The SQL query string
 * @param {Array|Object} binds Bind variables
 * @param {Object} options Execution options
 */
export async function executeQuery(sql, binds = [], options = {}) {
  let connection;
  try {
    if (!pool) {
      throw new Error('Database pool has not been initialized. Call initializeDb() first.');
    }
    connection = await pool.getConnection();
    
    // Request rows as JS objects (key-value pairs) instead of positional arrays
    options.outFormat = oracledb.OUT_FORMAT_OBJECT;
    // Auto-commit DML statements (INSERT, UPDATE, DELETE) by default
    options.autoCommit = options.autoCommit !== undefined ? options.autoCommit : true;
    
    const result = await connection.execute(sql, binds, options);
    return result;
  } catch (err) {
    console.error('SQL Execution Error:', err);
    throw err;
  } finally {
    if (connection) {
      try {
        await connection.close();
      } catch (err) {
        console.error('Error closing database connection:', err);
      }
    }
  }
}

export async function closeDb() {
  try {
    if (pool) {
      await pool.close();
      console.log('Oracle DB connection pool closed.');
    }
  } catch (err) {
    console.error('Error closing DB pool:', err);
  }
}
