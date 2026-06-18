#!/usr/bin/env bash
# Hook: check Java file conventions after edits
# Checks for common DolphinScheduler Java patterns

FILE="$1"
if [[ "$FILE" == *.java ]]; then
  # Check for Lombok usage reminders
  if ! grep -q "@Slf4j" "$FILE" 2>/dev/null && grep -q "log\." "$FILE" 2>/dev/null; then
    echo "💡 Reminder: Consider adding @Slf4j annotation for logging in $FILE"
  fi
  if grep -q "private static final Logger" "$FILE" 2>/dev/null; then
    echo "💡 Reminder: Use @Slf4j instead of manual Logger declaration in $FILE"
  fi
fi
