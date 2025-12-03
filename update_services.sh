#!/bin/bash
# Script to update all Service files with UUID conversion

cd /Users/user/Downloads/pt-java-persada-mandiri-erp/backend-java/domains/src/main/java/com/jpm/erp/domains

# Find all *Service.java files
find . -name "*Service.java" -type f | while read file; do
    echo "Processing: $file"
    
    # 1. Add UUID import if not present
    if ! grep -q "import java.util.UUID" "$file"; then
        sed -i '' '/^import java.util.List;/a\
import java.util.UUID;
' "$file"
    fi
    
    # 2. Add UUIDConverter import if not present
    if ! grep -q "import com.jpm.erp.platform.util.UUIDConverter" "$file"; then
        sed -i '' '/^import.*repository.*;/a\
import com.jpm.erp.platform.util.UUIDConverter;
' "$file"
    fi
    
    # 3. Convert findById(id) calls
    sed -i '' 's/\.findById(id)/\.findById(UUIDConverter.toUUID(id))/g' "$file"
    
    # 4. Convert existsById(id) calls
    sed -i '' 's/\.existsById(id)/\.existsById(UUIDConverter.toUUID(id))/g' "$file"
    
    # 5. Convert deleteById(id) calls  
    sed -i '' 's/\.deleteById(id)/\.deleteById(UUIDConverter.toUUID(id))/g' "$file"
    
    # 6. Convert .id(entity.getId()) in DTOs
    sed -i '' 's/\.id(entity\.getId())/\.id(UUIDConverter.toString(entity.getId()))/g' "$file"
    sed -i '' 's/\.id(e\.getId())/\.id(UUIDConverter.toString(e.getId()))/g' "$file"
    sed -i '' 's/\.id(p\.getId())/\.id(UUIDConverter.toString(p.getId()))/g' "$file"
done

echo "All Service files updated!"
