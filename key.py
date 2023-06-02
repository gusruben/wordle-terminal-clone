text = """
╭───┬───┬───┬───┬───┬───┬───┬───┬───┬───╮
│ q │ w │ e │ r │ t │ y │ u │ i │ o │ p │
├───┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴───┤
│   a │ s │ d │ f │ g │ h │ j │ k │ l   │
├─────┼───┼───┼───┼───┼───┼───┼───┼─────┤
│  ❯  │ z │ x │ c │ v │ b │ n │ m │  ❮  │
╰─────┴───┴───┴───┴───┴───┴───┴───┴─────╯
"""

chars = "abcdefghijklmnopqrstuvwxyz"
output = [""] * 26
l = 0
for line in text.strip().split("\n"):
	for i in range(len(chars)):
		char = chars[i]
		if char in line:
			output[i] = [l + 1, line.find(char)]
	l += 1

print("{")
for i in range(len(output)):
	c = output[i]
	print(f"\t{{{c[0]}, {c[1]}}},\t// {chars[i]}")
print("}")

