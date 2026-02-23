cd "$(dirname "$0")"
rm -rf classes
mkdir classes
javac -d classes $(find src -name "*.java")