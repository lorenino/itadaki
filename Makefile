.PHONY: run help

help:
	@echo "========================================"
	@echo "   ITADAKI - Make Commands"
	@echo "========================================"
	@echo ""
	@echo "make run          - Launch interactive menu"
	@echo ""

run:
	powershell -NoProfile -ExecutionPolicy Bypass -File "automation\menu.ps1"

