#!/usr/bin/env python3
"""Hestia AI API - HTTP bridge for JPM ERP"""
import json
import subprocess
import os
import re
from http.server import HTTPServer, BaseHTTPRequestHandler

MAX_TURNS = "5"
ENV = os.environ.copy()
ENV["HOME"] = "/home/hermes"
ENV["PATH"] = "/home/hermes/.hermes/hermes-agent/venv/bin:/usr/local/bin:/usr/bin:/bin"
ENV["PYTHONUNBUFFERED"] = "1"

class HestiaHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/chat":
            self.send_response(404)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"error": "Not Found"}).encode())
            return

        content_length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_length)
        try:
            data = json.loads(body)
        except:
            data = {}
        message = data.get("message", "")

        if not message:
            self.send_response(400)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"error": "Message required"}).encode())
            return

        cmd = [
            "/home/hermes/.hermes/hermes-agent/venv/bin/python",
            "-m", "hermes_cli.main",
            "chat",
            "-q", message,
            "-Q",
            "--max-turns", MAX_TURNS,
        ]

        try:
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=120,
                cwd="/home/hermes",
                env=ENV
            )

            output = result.stdout.strip()
            output = re.sub(r"^Warning:.*\n?", "", output, flags=re.MULTILINE)
            output = re.sub(r"\nsession_id:.*", "", output)
            output = re.sub(r"^Aborted.*\n?", "", output, flags=re.MULTILINE)
            output = output.strip()

            if output:
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"reply": output}).encode())
            else:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                err = result.stderr[:500] if result.stderr else "no output"
                self.wfile.write(json.dumps({"error": "Empty response", "stderr": err}).encode())

        except subprocess.TimeoutExpired:
            self.send_response(504)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"error": "Hestia request timed out"}).encode())
        except Exception as e:
            self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"error": str(e)}).encode())

    def log_message(self, format, *args):
        pass

if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 8765), HestiaHandler)
    print("Hestia AI API on http://0.0.0.0:8765")
    server.serve_forever()
