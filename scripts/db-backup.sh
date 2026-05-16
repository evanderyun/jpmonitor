#!/bin/bash
# PostgreSQL backup script for JpMonitor
BACKUP_DIR="${BACKUP_DIR:-/var/backups/jpmonitor}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-jpmonitor_db}"
DB_USER="${DB_USER:-jpm_user}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p "$BACKUP_DIR"
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -F c -f "$BACKUP_DIR/jpmonitor_$TIMESTAMP.dump"
gzip "$BACKUP_DIR/jpmonitor_$TIMESTAMP.dump"
find "$BACKUP_DIR" -name "*.dump.gz" -mtime +$RETENTION_DAYS -delete
echo "Backup completed: $BACKUP_DIR/jpmonitor_$TIMESTAMP.dump.gz"
