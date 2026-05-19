from models.task import AttackTask

class CredentialStuffingAttack:

    def __init__(self, queue_manager):
        self.queue_manager = queue_manager

    def load(self, combo_file):

        with open(combo_file, "r") as file:

            for line in file:

                combo = line.strip()

                if not combo:
                    continue

                email, password = combo.split(":")

                task = AttackTask(
                    email=email,
                    password=password
                )

                self.queue_manager.add_task(task)